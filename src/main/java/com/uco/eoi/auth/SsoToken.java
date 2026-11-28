package com.uco.eoi.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Reads the token the bank's SSO gateway puts on the URL as "Value".
 *
 * The token is AES/GCM/NoPadding, Base64 encoded, and laid out as
 * 12-byte IV + ciphertext + 16-byte tag. The key comes from the property
 * sso.encryption.key and may be 16, 24 or 32 bytes long.
 *
 * The decrypted text is a query string of the shape
 * "emp_id=E12345&amp;sol_id=7115" or "xx~E12345&amp;yy~7115".
 */
@Component
public class SsoToken {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    /**
     * Decrypts the token and returns the plain text.
     *
     * @throws IllegalArgumentException when the key, the token or the encoding is wrong
     */
    public String decrypt(String base64Token, String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalArgumentException("No SSO encryption key is configured.");
        }
        byte[] key = Base64.getDecoder().decode(base64Key.trim());
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("The SSO encryption key must be 16, 24 or 32 bytes.");
        }
        byte[] raw = Base64.getDecoder().decode(base64Token.trim());
        if (raw.length <= IV_BYTES) {
            throw new IllegalArgumentException("The SSO token is too short to hold a value.");
        }
        byte[] iv = new byte[IV_BYTES];
        System.arraycopy(raw, 0, iv, 0, IV_BYTES);
        byte[] body = new byte[raw.length - IV_BYTES];
        System.arraycopy(raw, IV_BYTES, body, 0, body.length);
        return plainText(key, iv, body);
    }

    /** Runs the cipher and turns a crypto failure into a plain refusal. */
    private String plainText(byte[] key, byte[] iv, byte[] body) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(body), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("The SSO token could not be read.", e);
        }
    }

    /**
     * The employee number inside the token text. Reads "emp_id=X" and "xx~X".
     */
    public String employeeNumber(String text) {
        return valueAfter(text, "emp_id", "xx");
    }

    /**
     * The SOL id inside the token text. Reads "sol_id=Y" and "yy~Y".
     */
    public String solId(String text) {
        return valueAfter(text, "sol_id", "yy");
    }

    /** Finds the value of one field, whichever of the two spellings the bank sent. */
    private String valueAfter(String text, String namedKey, String tildeKey) {
        if (text == null || text.isBlank()) {
            return "";
        }
        for (String part : text.split("&")) {
            String value = valueOfPart(part.trim(), namedKey, tildeKey);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    /** One "&"-separated piece of the token, read in either spelling. */
    private String valueOfPart(String part, String namedKey, String tildeKey) {
        if (part.startsWith(namedKey + "=")) {
            return part.substring(namedKey.length() + 1).trim();
        }
        if (part.startsWith(tildeKey + "~")) {
            return part.substring(tildeKey.length() + 1).trim();
        }
        return "";
    }
}

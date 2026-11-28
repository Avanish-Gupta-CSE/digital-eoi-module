package com.uco.eoi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Signing in: the bank SSO token, the dev one-time password, and a wrong password.
 *
 * The HRMS schema is empty here, so the lookup finds no officer and the dev
 * stand-in officer is used. That is the laptop case.
 */
class AuthControllerTest {

    private static final byte[] KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final String KEY_BASE64 = Base64.getEncoder().encodeToString(KEY);

    private MockMvc mockMvc() {
        // An empty schema means "no HRMS on this machine", which is the dev case.
        HrmsLookupService hrms = new HrmsLookupService(emptyJdbc(), "");
        AuthController controller = new AuthController(new SsoToken(), hrms,
                KEY_BASE64, true, "123456");
        return MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/jsp/", ".jsp"))
                .build();
    }

    /** A JdbcTemplate that is never called, because the schema is empty. */
    private JdbcTemplate emptyJdbc() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:mem:authtest_" + System.nanoTime() + ";MODE=Oracle");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return new JdbcTemplate(dataSource);
    }

    /** Encrypts plain text the way the bank gateway does, so the test can decrypt it. */
    private String encrypt(String plainText) {
        try {
            byte[] iv = "123456789012".getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"),
                    new GCMParameterSpec(128, iv));
            byte[] body = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] raw = new byte[iv.length + body.length];
            System.arraycopy(iv, 0, raw, 0, iv.length);
            System.arraycopy(body, 0, raw, iv.length, body.length);
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("the SSO token is read back as the employee number and the SOL id")
    void ssoToken_decrypts_theEmployeeNumberAndSolId() {
        SsoToken ssoToken = new SsoToken();

        String plainText = ssoToken.decrypt(encrypt("emp_id=E12345&sol_id=7115"), KEY_BASE64);

        assertThat(plainText).isEqualTo("emp_id=E12345&sol_id=7115");
        assertThat(ssoToken.employeeNumber(plainText)).isEqualTo("E12345");
        assertThat(ssoToken.solId(plainText)).isEqualTo("7115");
    }

    @Test
    @DisplayName("the SSO token in the tilde form is read the same way")
    void ssoToken_reads_theTildeForm() {
        SsoToken ssoToken = new SsoToken();

        String plainText = ssoToken.decrypt(encrypt("xx~E99999&yy~7103"), KEY_BASE64);

        assertThat(ssoToken.employeeNumber(plainText)).isEqualTo("E99999");
        assertThat(ssoToken.solId(plainText)).isEqualTo("7103");
    }

    @Test
    @DisplayName("a token signed with the wrong key is refused")
    void ssoToken_refuses_aWrongKey() {
        SsoToken ssoToken = new SsoToken();
        String otherKey = Base64.getEncoder()
                .encodeToString("fedcba9876543210".getBytes(StandardCharsets.UTF_8));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> ssoToken.decrypt(encrypt("emp_id=E12345"), otherKey))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the bank SSO link signs the officer in and opens the dashboard")
    void entry_signsTheOfficerIn() throws Exception {
        mockMvc().perform(get("/entry").param("Value", encrypt("emp_id=E12345&sol_id=7115")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("a broken SSO link sends the officer to the login screen")
    void entry_withABrokenToken_goesToTheLoginScreen() throws Exception {
        mockMvc().perform(get("/entry").param("Value", "not-a-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=sso"));
    }

    @Test
    @DisplayName("the correct one-time password signs the officer in")
    void login_withTheCorrectOtp_signsTheOfficerIn() throws Exception {
        mockMvc().perform(post("/login").param("empNo", "E12345").param("otp", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("a wrong one-time password is refused and the screen comes back")
    void login_withAWrongOtp_isRefused() throws Exception {
        mockMvc().perform(post("/login").param("empNo", "E12345").param("otp", "000000"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .view().name("login"));
    }
}

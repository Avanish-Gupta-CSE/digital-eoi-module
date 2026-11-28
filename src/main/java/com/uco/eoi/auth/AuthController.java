package com.uco.eoi.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Three ways in: the bank SSO link, the dev OTP form, and signing out.
 *
 * The signed-in officer is kept in the HTTP session under the name "staff".
 * A plain controller: read the input, call the service, redirect.
 */
@Controller
public class AuthController {

    /** The session attribute that holds the signed-in officer. */
    public static final String STAFF = "staff";

    private final SsoToken ssoToken;
    private final HrmsLookupService hrmsLookupService;
    private final String encryptionKey;
    private final boolean devLoginEnabled;
    private final String testOtp;
    private final LoginThrottle throttle;

    public AuthController(SsoToken ssoToken,
                          HrmsLookupService hrmsLookupService,
                          @Value("${sso.encryption.key:}") String encryptionKey,
                          @Value("${uco.sso.dev-login-enabled:false}") boolean devLoginEnabled,
                          @Value("${uco.sso.test-otp:123456}") String testOtp,
                          LoginThrottle throttle) {
        this.ssoToken = ssoToken;
        this.hrmsLookupService = hrmsLookupService;
        this.encryptionKey = encryptionKey;
        this.devLoginEnabled = devLoginEnabled;
        this.testOtp = testOtp;
        this.throttle = throttle;
    }

    /**
     * The bank SSO gateway sends the officer here with the token on the URL.
     */
    @GetMapping("/entry")
    public String entry(@RequestParam(name = "Value", required = false) String value,
                        HttpServletRequest request) {
        if (value == null || value.isBlank()) {
            return "redirect:/login?error=sso";
        }
        String plainText;
        try {
            plainText = ssoToken.decrypt(value, encryptionKey);
        } catch (IllegalArgumentException e) {
            return "redirect:/login?error=sso";
        }
        StaffPrincipal staff = staffFromToken(plainText);
        if (staff == null) {
            return "redirect:/login?error=sso";
        }
        signIn(request, staff);
        return "redirect:/dashboard";
    }

    /** Starts the officer's session under a new session id, so an old id cannot be reused. */
    private void signIn(HttpServletRequest request, StaffPrincipal staff) {
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(STAFF, staff);
    }

    /** Reads the officer out of the decrypted token, or null when HRMS has no row. */
    private StaffPrincipal staffFromToken(String plainText) {
        String empNo = ssoToken.employeeNumber(plainText);
        if (empNo.isEmpty()) {
            return null;
        }
        StaffPrincipal staff = hrmsLookupService.findByEmployeeNumber(empNo);
        return staff != null ? staff : devStaff(empNo);
    }

    /**
     * The dev OTP form. Closed in UAT, where the officer comes in through the bank SSO.
     */
    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) String error,
                        Model model) {
        if (!devLoginEnabled) {
            model.addAttribute("errorMessage",
                    "Sign in is through the bank single sign-on link. "
                            + "The dev OTP form is switched off on this server.");
            return "login";
        }
        if ("expired".equals(error)) {
            model.addAttribute("errorMessage",
                    "Your session has ended or the page was out of date. Please sign in again.");
        }
        if ("sso".equals(error)) {
            model.addAttribute("errorMessage",
                    "The bank sign-on link could not be read. Please sign in again with your "
                            + "employee number and the one-time password.");
        }
        return "login";
    }

    /**
     * Checks the one-time password and signs the officer in.
     */
    @PostMapping("/login")
    public String login(@RequestParam(name = "empNo", required = false) String empNo,
                        @RequestParam(name = "otp", required = false) String otp,
                        HttpServletRequest request,
                        Model model) {
        if (!devLoginEnabled) {
            model.addAttribute("errorMessage",
                    "The dev OTP form is switched off on this server.");
            return "login";
        }
        String address = request.getRemoteAddr();
        if (throttle.isLocked(address)) {
            model.addAttribute("errorMessage",
                    "Too many wrong attempts. Wait 15 minutes and try again.");
            return "login";
        }
        if (empNo == null || empNo.isBlank()) {
            model.addAttribute("errorMessage", "Enter your employee number.");
            return "login";
        }
        if (!sameText(testOtp, otp == null ? "" : otp.trim())) {
            throttle.recordFailure(address);
            model.addAttribute("errorMessage",
                    "The one-time password is not correct. Check the number and try again.");
            return "login";
        }
        StaffPrincipal staff = hrmsLookupService.findByEmployeeNumber(empNo.trim());
        if (staff == null) {
            staff = devStaff(empNo.trim());
        }
        if (staff == null) {
            model.addAttribute("errorMessage",
                    "No officer is found with employee number " + empNo.trim()
                            + ". Check the number with the HRMS team.");
            return "login";
        }
        throttle.recordSuccess(address);
        signIn(request, staff);
        return "redirect:/dashboard";
    }

    /** Compares two texts in constant time, so the answer time does not leak the password. */
    private static boolean sameText(String expected, String given) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                given.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * A stand-in officer for a laptop with no HRMS. Only used when the dev login is on.
     */
    private StaffPrincipal devStaff(String empNo) {
        if (!devLoginEnabled) {
            return null;
        }
        return new StaffPrincipal(empNo, "Dev Officer", "7115", "V", "Credit Officer",
                StaffPrincipal.CHECKER);
    }

    /**
     * Signs the officer out and ends the session.
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

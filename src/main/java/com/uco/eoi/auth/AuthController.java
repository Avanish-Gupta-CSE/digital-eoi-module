package com.uco.eoi.auth;

import jakarta.servlet.http.HttpSession;
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

    public AuthController(SsoToken ssoToken,
                          HrmsLookupService hrmsLookupService,
                          @Value("${sso.encryption.key:}") String encryptionKey,
                          @Value("${uco.sso.dev-login-enabled:false}") boolean devLoginEnabled,
                          @Value("${uco.sso.test-otp:123456}") String testOtp) {
        this.ssoToken = ssoToken;
        this.hrmsLookupService = hrmsLookupService;
        this.encryptionKey = encryptionKey;
        this.devLoginEnabled = devLoginEnabled;
        this.testOtp = testOtp;
    }

    /**
     * The bank SSO gateway sends the officer here with the token on the URL.
     */
    @GetMapping("/entry")
    public String entry(@RequestParam(name = "Value", required = false) String value,
                        HttpSession session) {
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
        session.setAttribute(STAFF, staff);
        return "redirect:/dashboard";
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
                        HttpSession session,
                        Model model) {
        if (!devLoginEnabled) {
            model.addAttribute("errorMessage",
                    "The dev OTP form is switched off on this server.");
            return "login";
        }
        if (empNo == null || empNo.isBlank()) {
            model.addAttribute("errorMessage", "Enter your employee number.");
            return "login";
        }
        if (!testOtp.equals(otp == null ? "" : otp.trim())) {
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
        session.setAttribute(STAFF, staff);
        return "redirect:/dashboard";
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

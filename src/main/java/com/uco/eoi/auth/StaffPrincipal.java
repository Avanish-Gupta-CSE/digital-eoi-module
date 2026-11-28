package com.uco.eoi.auth;

/**
 * The signed-in bank officer. Kept in the HTTP session under the name "staff".
 *
 * A record, so it cannot change after it is put in the session: a role read at
 * sign-in is the role used for every later action.
 */
public record StaffPrincipal(
        String empNo,
        String name,
        String solId,
        String scaleCode,
        String designation,
        String role) {

    /** The maker role: prepares and submits a report. */
    public static final String MAKER = "MAKER";

    /** The checker role: approves or returns a report. */
    public static final String CHECKER = "CHECKER";

    /** The two initials shown in the header circle, for example "RS" for Ramesh Sharma. */
    public String initials() {
        String[] parts = (name == null ? "" : name).trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return "?";
        }
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    /** True when this officer may check a report. */
    public boolean isChecker() {
        return CHECKER.equals(role);
    }
}

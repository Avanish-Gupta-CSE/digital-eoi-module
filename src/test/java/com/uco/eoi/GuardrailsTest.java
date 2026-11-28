package com.uco.eoi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

/**
 * Reads the source and fails when a rule of .opencode/guardrails/security-and-integrity.md
 * is broken in a way a grep cannot see: a write URL the report lock does not cover, and a
 * DELETE that is not tied to its parent report.
 */
class GuardrailsTest {

    /** The maker-checker actions guard themselves in WorkflowService, so the lock skips them. */
    private static final Set<String> WORKFLOW_URLS = Set.of(
            "/reports/{id}/submit", "/reports/{id}/approve", "/reports/{id}/return", "/reports/{id}/reopen",
            "/reports/{id}/sync-finacle");

    private static final Path JAVA = Path.of("src/main/java");

    @Test
    @DisplayName("every POST under /reports/{id}/ is frozen by ReportLockConfig (rule 6)")
    void everyReportWriteUrl_isCoveredByTheLock() throws IOException {
        List<String> patterns = quoted(read("com/uco/eoi/report/ReportLockConfig.java"),
                "\"(/reports/\\*/[^\"]*)\"");
        AntPathMatcher matcher = new AntPathMatcher();
        List<String> uncovered = new ArrayList<>();
        for (String url : postUrls()) {
            if (!url.startsWith("/reports/{id}/") || WORKFLOW_URLS.contains(url)) {
                continue;
            }
            String concrete = url.replace("{id}", "1").replace("{rowId}", "2");
            boolean covered = patterns.stream().anyMatch(p -> matcher.match(p, concrete));
            if (!covered) {
                uncovered.add(url);
            }
        }
        assertThat(uncovered)
                .as("add these URLs to ReportLockConfig, or a submitted report can still be edited")
                .isEmpty();
    }

    @Test
    @DisplayName("every DELETE is tied to its parent report or borrower (rule 5)")
    void everyDelete_namesItsParent() throws IOException {
        List<String> loose = new ArrayList<>();
        try (Stream<Path> files = Files.walk(JAVA)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String text = Files.readString(file);
                Matcher m = Pattern.compile("\"(DELETE FROM[^\"]*)\"(?:\\s*\\+\\s*\"([^\"]*)\")*")
                        .matcher(text);
                while (m.find()) {
                    String sql = m.group();
                    boolean tied = sql.contains("report_id") || sql.contains("borrower_id");
                    boolean child = sql.contains("facility_id IN");
                    if (!tied && !child && !sql.contains("eoi_audit")) {
                        loose.add(file.getFileName() + ": " + m.group(1));
                    }
                }
            }
        }
        assertThat(loose)
                .as("a DELETE by row id alone lets one report's row be removed through another's URL")
                .isEmpty();
    }

    private static List<String> postUrls() throws IOException {
        List<String> urls = new ArrayList<>();
        try (Stream<Path> files = Files.walk(JAVA)) {
            for (Path file : files.filter(f -> f.toString().endsWith("Controller.java")).toList()) {
                urls.addAll(quoted(Files.readString(file), "@PostMapping\\(\"([^\"]*)\"\\)"));
            }
        }
        return urls;
    }

    private static String read(String relative) throws IOException {
        return Files.readString(JAVA.resolve(relative));
    }

    private static List<String> quoted(String text, String regex) {
        List<String> found = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) {
            found.add(m.group(1));
        }
        return found;
    }
}

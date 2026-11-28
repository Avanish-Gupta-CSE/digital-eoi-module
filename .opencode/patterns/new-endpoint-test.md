# Test for an endpoint

Copy: `ReportLockTest` (H2 with the real scripts, one fresh database per test) and
`SecurityFiltersTest`.

```java
@Test @DisplayName("an approved report cannot be changed through this URL")
void change_ofAnApprovedReport_isRefused() throws Exception {
    long id = firstReportId();
    workflow.submit(id, "maker01");
    workflow.approve(id, "checker01");
    assertThat(post("/reports/" + id + "/things/add").getRedirectedUrl())
            .isEqualTo("/reports/" + id + "?error=locked");
}
```

Write all four: wrong role, wrong status, wrong owner, no token. A test that passes before your
change proves nothing: run it once against the unfixed code and see it fail.

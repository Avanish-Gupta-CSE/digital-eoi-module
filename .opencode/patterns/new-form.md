# New form in a JSP

Copy: the add-currency form in `fragments/edit-part5-6.jspf`.

```jsp
<form method="post" action="${pageContext.request.contextPath}/reports/${view.reportId()}/things/add">
    <input type="hidden" name="_csrf" value="<c:out value='${sessionScope._csrf}'/>">
    <label class="form-label" for="thing-name">Name *</label>
    <input class="form-control" id="thing-name" name="name" type="text" required>
    <button type="submit" class="btn-bank-primary" id="add-thing-button">Add</button>
</form>
```

- The `_csrf` line is not optional (rule 2). `sh scripts/check-guardrails.sh` fails without it.
- No `style="..."`, no `<script>`. Behaviour goes in `assets/js/<page>.js`, bound by element id.
- Print values back with `<c:out>` (rule 1), including in `value="..."`.
- Show the form only in editable statuses (see `report-workflow.jspf` for the `makerCanAct` idea).

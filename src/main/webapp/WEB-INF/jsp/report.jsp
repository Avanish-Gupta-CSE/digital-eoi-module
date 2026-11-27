<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="app-main">

    <div class="page-head">
        <div>
            <p class="page-eyebrow">Annex II report</p>
            <h1 class="page-title">${view.borrower.name}</h1>
            <p class="page-subtitle">Quarter ${view.quarter} &middot; ${view.borrower.consortiumName}</p>
        </div>
        <div class="page-actions">
            <a class="btn-print" href="/dashboard">Back to dashboard</a>
            <button type="button" id="print-button" class="btn-print">Print</button>
        </div>
    </div>

    <p class="report-status-line">
        <c:choose>
            <c:when test="${view.status == 'APPROVED'}">
                <span class="status-pill status-approved">${view.status}</span>
            </c:when>
            <c:when test="${view.status == 'SUBMITTED'}">
                <span class="status-pill status-submitted">${view.status}</span>
            </c:when>
            <c:when test="${view.status == 'RETURNED'}">
                <span class="status-pill status-returned">${view.status}</span>
            </c:when>
            <c:when test="${view.status == 'DUE'}">
                <span class="status-pill status-due">${view.status}</span>
            </c:when>
            <c:when test="${view.status == 'OVERDUE'}">
                <span class="status-pill status-overdue">${view.status}</span>
            </c:when>
            <c:otherwise>
                <span class="status-pill status-draft">${view.status}</span>
            </c:otherwise>
        </c:choose>
    </p>

    <%@ include file="fragments/report-part1-3.jspf" %>
    <%@ include file="fragments/report-part4-6.jspf" %>

</main>

<%@ include file="fragments/foot.jspf" %>
</body>
</html>

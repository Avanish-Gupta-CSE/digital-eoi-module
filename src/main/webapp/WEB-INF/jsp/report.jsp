<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="app-main">

    <div class="page-head">
        <div>
            <p class="page-eyebrow">Annex II report</p>
            <h1 class="page-title"><c:out value="${view.borrower.name}"/></h1>
            <p class="page-subtitle">Quarter <c:out value="${view.quarter}"/> &middot; <c:out value="${view.borrower.consortiumName}"/></p>
        </div>
        <div class="page-actions">
            <a class="btn btn-primary btn-bank-primary" href="/reports/${view.reportId()}/edit">Edit Annex II</a>
            <a class="btn-print" href="/reports/${view.reportId()}/export.csv">Export CSV</a>
            <a class="btn-print" href="/dashboard">Back to dashboard</a>
            <button type="button" id="print-button" class="btn-print">Print</button>
        </div>
    </div>

    <c:if test="${param.saved == '1'}">
        <div class="alert alert-success alert-dismissible fade show" role="status" data-auto-fade>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            Annex II for <c:out value="${view.quarter}"/> saved.
        </div>
    </c:if>

    <c:if test="${not empty param.error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            <c:out value="${not empty errorMessage ? errorMessage : param.error}"/>
        </div>
    </c:if>

    <c:if test="${not empty param.workflow}">
        <div class="alert alert-success alert-dismissible fade show" role="status" data-auto-fade>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            <c:out value="${param.workflow}"/>
        </div>
    </c:if>

    <%@ include file="fragments/report-status.jspf" %>

    <%@ include file="fragments/report-workflow.jspf" %>

    <%@ include file="fragments/report-part1-3.jspf" %>
    <%@ include file="fragments/report-part4-6.jspf" %>

</main>

<%@ include file="fragments/foot.jspf" %>

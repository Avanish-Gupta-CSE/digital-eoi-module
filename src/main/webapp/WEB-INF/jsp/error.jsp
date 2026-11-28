<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="app-main">
    <div class="data-card entry-card">
        <p class="page-eyebrow">Digital EoI</p>
        <h1 class="page-title">Something went wrong</h1>
        <p class="page-subtitle">
            The request could not be completed (status <c:out value="${status}"/>).
            Go back to the dashboard and try again.
        </p>
        <div class="entry-actions">
            <a class="btn btn-primary btn-bank-primary"
               href="${pageContext.request.contextPath}/dashboard">Back to Dashboard</a>
        </div>
    </div>
</main>

<%@ include file="fragments/foot.jspf" %>

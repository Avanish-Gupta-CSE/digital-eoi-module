<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="container py-4">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
            <li class="breadcrumb-item">Home</li>
            <li class="breadcrumb-item">EoI</li>
            <li class="breadcrumb-item active" aria-current="page">Quarterly dashboard</li>
        </ol>
    </nav>

    <div class="d-flex align-items-start justify-content-between mb-3">
        <div>
            <h1 class="page-title">Quarterly EoI dashboard</h1>
            <p class="page-subtitle">Borrowers due for exchange in <strong>${view.quarter}</strong>.</p>
        </div>
        <button type="button" id="print-button" class="btn btn-outline-secondary">Print</button>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <span class="stat-label">Borrowers</span>
                <span class="stat-value">${view.total}</span>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <span class="stat-label">Due</span>
                <span class="stat-value">${view.dueCount}</span>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <span class="stat-label">Submitted</span>
                <span class="stat-value">${view.submittedCount}</span>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="stat-card">
                <span class="stat-label">Overdue</span>
                <span class="stat-value">${view.overdueCount}</span>
            </div>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty view.rows}">
            <div class="empty-state">
                <h2 class="empty-title">No borrowers are due this quarter</h2>
                <p class="empty-text">
                    Nothing matches the current threshold for ${view.quarter}.
                    The next quarterly window opens on the first day of the next quarter.
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover align-middle">
                    <thead>
                    <tr>
                        <th scope="col">Borrower</th>
                        <th scope="col">Consortium</th>
                        <th scope="col" class="text-end">Sanctioned limit</th>
                        <th scope="col" class="text-end">Outstanding</th>
                        <th scope="col" class="text-center">Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${view.rows}" var="row">
                        <tr>
                            <td>${row.borrowerName}</td>
                            <td>${row.consortiumName}</td>
                            <td class="text-end amount">${row.sanctionedLimitText}</td>
                            <td class="text-end amount">${row.outstandingText}</td>
                            <td class="text-center">
                                <span class="badge ${row.statusClass}">${row.status}</span>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="fragments/foot.jspf" %>

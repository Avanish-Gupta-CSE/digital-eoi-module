<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="app-main">

    <div class="page-head">
        <div>
            <div class="page-eyebrow">Quarterly return</div>
            <h1 class="page-title">Borrowers due for exchange</h1>
            <p class="page-subtitle">
                Consortium and multiple banking borrowers above the reporting threshold
                for <strong>${view.quarter}</strong>.
            </p>
        </div>
        <div class="page-actions">
            <span class="quarter-chip"><span class="dot"></span>${view.quarter}</span>
            <button type="button" id="print-button" class="btn-print">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2M6 14h12v8H6z"/>
                </svg>
                Print
            </button>
        </div>
    </div>

    <%@ include file="fragments/kpi.jspf" %>

    <c:choose>
        <c:when test="${empty view.rows}">
            <div class="data-card">
                <div class="empty-state">
                    <div class="empty-icon" aria-hidden="true">
                        <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                            <path d="M14 2v6h6M9 15h6"/>
                        </svg>
                    </div>
                    <h2 class="empty-title">No borrowers are due this quarter</h2>
                    <p class="empty-text">
                        Nothing matches the current threshold for ${view.quarter}.
                        The next quarterly window opens on the first day of the next quarter.
                    </p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <section class="data-card">
                <div class="data-card-head">
                    <h2 class="data-card-title">
                        Borrowers
                        <span class="count-pill" id="visible-count">${view.total}</span>
                    </h2>
                    <div class="toolbar">
                        <div class="search-box">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>
                            </svg>
                            <input type="search" id="borrower-search" placeholder="Search borrower or consortium"
                                   aria-label="Search borrower or consortium">
                        </div>
                        <div class="filter-chips" role="group" aria-label="Filter by status">
                            <button type="button" class="chip is-active" data-filter="ALL">All</button>
                            <button type="button" class="chip" data-filter="DUE">Due</button>
                            <button type="button" class="chip" data-filter="SUBMITTED">Submitted</button>
                            <button type="button" class="chip" data-filter="OVERDUE">Overdue</button>
                        </div>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="eoi-table">
                        <thead>
                        <tr>
                            <th scope="col">Borrower</th>
                            <th scope="col" class="text-end">Sanctioned limit</th>
                            <th scope="col" class="text-end">Outstanding</th>
                            <th scope="col" class="text-center">Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${view.rows}" var="row">
                            <tr data-row data-status="${row.status}">
                                <td>
                                    <div class="borrower-cell">
                                        <span class="borrower-initials" aria-hidden="true">${row.initials}</span>
                                        <span>
                                            <a class="borrower-name" href="/reports/${row.reportId}">${row.borrowerName}</a>
                                            <span class="borrower-sub">${row.consortiumName}</span>
                                        </span>
                                    </div>
                                </td>
                                <td class="text-end amount">${row.sanctionedLimitText}</td>
                                <td class="text-end amount">${row.outstandingText}</td>
                                <td class="text-center">
                                    <span class="status-pill ${row.statusClass}">
                                        <span class="dot" aria-hidden="true"></span>${row.status}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:otherwise>
    </c:choose>

</main>

<%@ include file="fragments/foot.jspf" %>

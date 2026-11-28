<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ include file="fragments/head.jspf" %>
<%@ include file="fragments/nav.jspf" %>

<main class="app-main">

    <div class="page-head">
        <div>
            <p class="page-eyebrow">Annex II report</p>
            <h1 class="page-title">Annex II Entry</h1>
            <p class="page-subtitle">${view.borrower.name} &middot; Quarter ${view.quarter}</p>
        </div>
        <div class="page-actions">
            <form method="post" action="${pageContext.request.contextPath}/reports/${view.reportId()}/prefill">
                <button type="submit" class="btn-print">Prefill from Previous Quarter</button>
            </form>
        </div>
    </div>

    <%@ include file="fragments/report-status.jspf" %>

    <c:if test="${param.saved == '1'}">
        <div class="alert alert-success alert-dismissible fade show" role="status">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            Entry saved.
        </div>
    </c:if>

    <c:if test="${param.prefilled == '1'}">
        <div class="alert alert-success alert-dismissible fade show" role="status">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            Details copied from the report for ${empty param.prefillQuarter ? 'the previous quarter' : param.prefillQuarter}. Check every value before saving.
        </div>
    </c:if>

    <c:if test="${param.prefill == 'none'}">
        <div class="alert alert-warning" role="status">
            No previous quarter report exists for this borrower. Enter the details for this quarter.
        </div>
    </c:if>

    <form:form id="report-edit-form" method="post" modelAttribute="form"
               action="${pageContext.request.contextPath}/reports/${view.reportId()}/edit">
        <div class="data-card entry-card">
            <p class="entry-required">* Required</p>

            <fieldset class="entry-part">
                <legend>Part I &mdash; Bio Data of the Company</legend>
                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="address">Address *</label>
                        <form:input path="address" cssClass="form-control"/>
                        <form:errors path="address" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="constitution">Constitution *</label>
                        <form:input path="constitution" cssClass="form-control"/>
                        <form:errors path="constitution" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="mainActivity">Business activity &mdash; Main *</label>
                        <form:input path="mainActivity" cssClass="form-control"/>
                        <form:errors path="mainActivity" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="groupAffiliation">Group affiliation</label>
                        <form:input path="groupAffiliation" cssClass="form-control"/>
                        <form:errors path="groupAffiliation" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="associateConcerns">Associate concerns</label>
                        <form:textarea path="associateConcerns" rows="3" cssClass="form-control"/>
                        <form:errors path="associateConcerns" cssClass="text-danger small d-block"/>
                    </div>
                </div>
            </fieldset>

            <fieldset class="entry-part">
                <legend>Part II &mdash; Major credit quality indicators</legend>
                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="iracClassification">IRAC classification</label>
                        <form:input path="iracClassification" cssClass="form-control"/>
                        <form:errors path="iracClassification" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="internalRating">Internal rating</label>
                        <form:input path="internalRating" cssClass="form-control"/>
                        <form:errors path="internalRating" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="internalRatingNarration">Internal rating narration</label>
                        <form:textarea path="internalRatingNarration" rows="3" cssClass="form-control"/>
                        <form:errors path="internalRatingNarration" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="externalRating">External rating</label>
                        <form:input path="externalRating" cssClass="form-control"/>
                        <form:errors path="externalRating" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="annualReportLatest">Latest annual report</label>
                        <form:input path="annualReportLatest" cssClass="form-control"/>
                        <form:errors path="annualReportLatest" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="annualReportAsOn">Annual report as on</label>
                        <form:input path="annualReportAsOn" cssClass="form-control" placeholder="dd-mm-yyyy"/>
                        <form:errors path="annualReportAsOn" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="changesShareholding">Changes in shareholding</label>
                        <form:textarea path="changesShareholding" rows="3" cssClass="form-control"/>
                        <form:errors path="changesShareholding" cssClass="text-danger small d-block"/>
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="specialInformation">Special information</label>
                        <form:textarea path="specialInformation" rows="3" cssClass="form-control"/>
                        <form:errors path="specialInformation" cssClass="text-danger small d-block"/>
                    </div>
                </div>
            </fieldset>

            <div class="entry-actions">
                <button type="submit" class="btn btn-primary btn-bank-primary" id="save-draft-button">
                    <span class="spinner-border spinner-border-sm d-none" id="save-spinner" aria-hidden="true"></span>
                    <span id="save-button-label">Save Draft</span>
                </button>
                <a class="btn-print" href="${pageContext.request.contextPath}/reports/${view.reportId()}">Cancel</a>
            </div>
        </div>
    </form:form>

    <%@ include file="fragments/edit-part1-children.jspf" %>
    <%@ include file="fragments/edit-part3.jspf" %>
    <%@ include file="fragments/edit-part4.jspf" %>
    <%@ include file="fragments/edit-part5-6.jspf" %>
    <%@ include file="fragments/edit-part6-rubric.jspf" %>

</main>

<%@ include file="fragments/foot.jspf" %>

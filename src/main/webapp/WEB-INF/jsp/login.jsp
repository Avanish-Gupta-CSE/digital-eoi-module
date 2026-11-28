<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="fragments/head.jspf" %>
<c:set var="hideStaff" value="true"/>
<%@ include file="fragments/nav.jspf" %>

<main class="login-main">

    <div class="data-card login-card">
        <h1 class="login-title">Sign in to the Digital EoI module</h1>
        <p class="login-subtitle">
            For development and UAT only. In production the officer comes in through the
            bank single sign-on link.
        </p>

        <c:if test="${not empty errorMessage}">
            <p class="login-error" role="alert"><c:out value="${errorMessage}"/></p>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="_csrf" value="<c:out value='${sessionScope._csrf}'/>">
            <div class="login-field">
                <label for="empNo">Employee number</label>
                <input type="text" id="empNo" name="empNo" autocomplete="username"
                       maxlength="20" required
                       value="<c:out value='${param.empNo}'/>">
            </div>

            <div class="login-field">
                <label for="otp">One-time password</label>
                <input type="password" id="otp" name="otp" autocomplete="one-time-code"
                       maxlength="12" required>
            </div>

            <div class="entry-actions">
                <button type="submit" class="btn-bank-primary">Sign in</button>
            </div>
        </form>

        <p class="login-note">
            The one-time password is sent to the mobile number registered in the HRMS.
            If it does not arrive, contact the HRMS team with your employee number.
        </p>
    </div>

</main>

<%@ include file="fragments/foot.jspf" %>

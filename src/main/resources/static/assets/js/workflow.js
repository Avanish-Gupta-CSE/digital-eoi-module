// Maker and checker actions on the Annex II report screen.
// Bound by element id because inline <script> is forbidden by the bank CSP.

document.addEventListener("DOMContentLoaded", function () {
    const submitButton = document.getElementById("submit-button");
    const approveButton = document.getElementById("approve-button");
    const returnForm = document.getElementById("return-form");
    const remarks = document.getElementById("return-remarks");
    const remarksError = document.getElementById("return-remarks-error");
    const remarkMessage = "Enter a reason for returning the report, at least 10 characters.";

    function confirmSend(button, message) {
        if (!button) {
            return;
        }
        button.addEventListener("click", function (event) {
            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    }

    function showRemarkError() {
        remarksError.textContent = remarkMessage;
        remarksError.classList.replace("d-none", "d-block");
        remarks.classList.add("is-invalid");
        remarks.setAttribute("aria-invalid", "true");
        remarks.focus();
    }

    function clearRemarkError() {
        remarksError.classList.replace("d-block", "d-none");
        remarks.classList.remove("is-invalid");
        remarks.removeAttribute("aria-invalid");
    }

    confirmSend(submitButton, "Submit this report for checking?");
    confirmSend(approveButton, "Approve this report?");

    if (!returnForm || !remarks || !remarksError) {
        return;
    }

    returnForm.addEventListener("submit", function (event) {
        if (remarks.value.trim().length < 10) {
            event.preventDefault();
            showRemarkError();
            return;
        }
        clearRemarkError();
    });

    remarks.addEventListener("input", clearRemarkError);
});

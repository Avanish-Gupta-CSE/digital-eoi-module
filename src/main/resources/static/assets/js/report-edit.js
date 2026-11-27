// Dirty-form warning and save state for the Annex II entry screen.
// Bound by element id because inline <script> is forbidden by the bank CSP.

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("report-edit-form");
    if (!form) {
        return;
    }

    const saveButton = document.getElementById("save-draft-button");
    const saveLabel = document.getElementById("save-button-label");
    const spinner = document.getElementById("save-spinner");
    let dirty = false;
    let submitting = false;

    form.addEventListener("input", function () {
        dirty = true;
    });

    form.addEventListener("change", function () {
        dirty = true;
    });

    form.addEventListener("submit", function () {
        submitting = true;
        if (saveButton) {
            saveButton.disabled = true;
        }
        if (saveLabel) {
            saveLabel.textContent = "Saving…";
        }
        if (spinner) {
            spinner.classList.remove("d-none");
        }
    });

    window.addEventListener("beforeunload", function (event) {
        if (!dirty || submitting) {
            return;
        }
        event.preventDefault();
        event.returnValue = "";
    });
});

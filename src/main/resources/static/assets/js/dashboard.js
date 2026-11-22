// Event handlers only. No business logic lives in the browser.
// Every handler is bound by element id, because inline <script> is forbidden by the bank CSP.

document.addEventListener("DOMContentLoaded", function () {
    const printButton = document.getElementById("print-button");
    if (printButton) {
        printButton.addEventListener("click", function () {
            window.print();
        });
    }
});

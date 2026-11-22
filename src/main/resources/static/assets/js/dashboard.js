// Event handlers only. No business logic in the browser.
// Bound by element id because inline <script> is forbidden by the bank CSP.

document.addEventListener("DOMContentLoaded", function () {
    const rows = Array.from(document.querySelectorAll("[data-row]"));
    const search = document.getElementById("borrower-search");
    const chips = Array.from(document.querySelectorAll("[data-filter]"));
    const counter = document.getElementById("visible-count");
    let activeStatus = "ALL";

    function applyFilters() {
        const query = (search && search.value ? search.value : "").trim().toLowerCase();
        let visible = 0;

        rows.forEach(function (row) {
            const status = row.getAttribute("data-status");
            const matchesStatus = activeStatus === "ALL" || status === activeStatus;
            const matchesText = query === "" || row.textContent.toLowerCase().indexOf(query) !== -1;
            const show = matchesStatus && matchesText;
            row.hidden = !show;
            if (show) {
                visible++;
            }
        });

        if (counter) {
            counter.textContent = visible;
        }
    }

    if (search) {
        search.addEventListener("input", applyFilters);
    }

    chips.forEach(function (chip) {
        chip.addEventListener("click", function () {
            chips.forEach(function (other) {
                other.classList.remove("is-active");
            });
            chip.classList.add("is-active");
            activeStatus = chip.getAttribute("data-filter");
            applyFilters();
        });
    });

    const printButton = document.getElementById("print-button");
    if (printButton) {
        printButton.addEventListener("click", function () {
            window.print();
        });
    }
});

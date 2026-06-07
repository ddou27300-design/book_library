document.addEventListener("DOMContentLoaded", function () {
    // --- Sidebar Toggle ---
    const toggleBtn = document.getElementById("sidebarToggle");
    const wrapper = document.querySelector(".admin-wrapper");

    if (toggleBtn && wrapper) {
        toggleBtn.addEventListener("click", function (e) {
            e.preventDefault();

            if (window.innerWidth >= 992) {
                wrapper.classList.toggle("sidebar-collapsed");
            } else {
                wrapper.classList.toggle("sidebar-mobile-open");
            }
        });
    }

    // --- Theme Toggle ---
    const themeToggle = document.getElementById("themeToggle");
    const sunIcon = document.getElementById("themeSun");
    const moonIcon = document.getElementById("themeMoon");

    // Check if user previously saved a layout theme preference
    const currentTheme = localStorage.getItem("admin-theme");

    if (currentTheme === "light") {
        document.body.classList.add("light-theme");
        if (sunIcon) sunIcon.classList.remove("d-none");
        if (moonIcon) moonIcon.classList.add("d-none");
    } else {
        if (sunIcon) sunIcon.classList.add("d-none");
        if (moonIcon) moonIcon.classList.remove("d-none");
    }

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            document.body.classList.toggle("light-theme");

            let theme = "dark";
            if (document.body.classList.contains("light-theme")) {
                theme = "light";
                sunIcon.classList.remove("d-none");
                moonIcon.classList.add("d-none");
            } else {
                sunIcon.classList.add("d-none");
                moonIcon.classList.remove("d-none");
            }

            // Persist theme selection so page reloads don't reset it
            localStorage.setItem("admin-theme", theme);
        });
    }
});

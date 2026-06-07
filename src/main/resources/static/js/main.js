document.addEventListener("DOMContentLoaded", function () {
    // --- Public Site Theme Toggle ---
    const userToggle = document.getElementById("userThemeToggle");
    const sunIcon = document.getElementById("userThemeSun");
    const moonIcon = document.getElementById("userThemeMoon");

    // Read cached layout preference configuration state
    const currentTheme = localStorage.getItem("admin-theme");

    if (currentTheme === "light") {
        document.body.classList.add("light-theme");
        if (sunIcon) sunIcon.classList.remove("d-none");
        if (moonIcon) moonIcon.classList.add("d-none");
    } else {
        if (sunIcon) sunIcon.classList.add("d-none");
        if (moonIcon) moonIcon.classList.remove("d-none");
    }

    if (userToggle) {
        userToggle.addEventListener("click", function () {
            document.body.classList.toggle("light-theme");

            let theme = "dark";
            if (document.body.classList.contains("light-theme")) {
                theme = "light";
                if (sunIcon) sunIcon.classList.remove("d-none");
                if (moonIcon) moonIcon.classList.add("d-none");
            } else {
                if (sunIcon) sunIcon.classList.add("d-none");
                if (moonIcon) moonIcon.classList.remove("d-none");
            }
            localStorage.setItem("admin-theme", theme);
        });
    }
});

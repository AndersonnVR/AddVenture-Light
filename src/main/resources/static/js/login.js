function togglePassword(id) {
    const input = document.getElementById(id);
    const icon = input.nextElementSibling.querySelector("i");
    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("bi-eye", "bi-eye-slash");
    } else {
        input.type = "password";
        icon.classList.replace("bi-eye-slash", "bi-eye");
    }
}
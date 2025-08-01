document.addEventListener('DOMContentLoaded', function () {
    // Seleccionar los elementos con la clase 'alert-auto'
    var alerts = document.querySelectorAll('.alert.alert-auto');
    // Recorrer los elementos seleccionados
    alerts.forEach(function (alert) {
        // Código a ejecutar después de 5 segundos
        setTimeout(function () {
            // Crear instancia de alerta de Bootstrap
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});

function copiarEnlaceWhatsapp() {
    const enlace = document.querySelector('a[href^="https://chat.whatsapp.com"]');
    if (enlace) {
        navigator.clipboard.writeText(enlace.href).then(function () {
            alert("¡Enlace copiado al portapapeles! ✅");
        }, function (err) {
            alert("No se pudo copiar el enlace. ❌");
        });
    }
}
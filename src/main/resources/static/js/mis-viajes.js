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

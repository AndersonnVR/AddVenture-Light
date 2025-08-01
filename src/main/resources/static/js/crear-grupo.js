document.addEventListener('DOMContentLoaded', function () {

    const form = document.getElementById('grupoViajeForm');
    if (!form) return;

    // --- ELEMENTOS DEL DOM (igual que en tu archivo original) ---
    const fechaInicioInput = document.getElementById('fechaInicio');
    const fechaFinInput = document.getElementById('fechaFin');
    const tripDaysBadge = document.getElementById('trip-days-badge');
    const generarItinerarioBtn = document.getElementById('generarItinerarioBtn');
    const itineraryAccordion = document.getElementById('itineraryAccordion');
    const noDatesWarning = document.getElementById('no-dates-warning');
    const etiquetasInput = document.getElementById('etiquetasInput');
    const etiquetasError = document.getElementById('etiquetasError'); // Nuevo div para errores
    const rangoEdadMin = document.getElementById('rangoEdadMin');
    const rangoEdadMax = document.getElementById('rangoEdadMax');
    const itineraryTemplate = document.getElementById('itinerary-day-template'); // Usaremos el template del HTML

    // --- FUNCIONES (mantenemos tus funciones vitales y las mejoramos un poco) ---

    // Función para calcular días entre fechas (sin cambios, estaba perfecta)
    function calcularDiasViaje() {
        if (fechaInicioInput.value && fechaFinInput.value) {
            const inicio = new Date(fechaInicioInput.value);
            const fin = new Date(fechaFinInput.value);
            if (fin < inicio) return 0;
            const diferencia = Math.ceil((fin - inicio) / (1000 * 60 * 60 * 24)) + 1;
            return diferencia > 0 ? diferencia : 0;
        }
        return 0;
    }

    // Función para actualizar el badge de días de viaje (sin cambios)
    function actualizarDiasBadge() {
        const dias = calcularDiasViaje();
        if (tripDaysBadge) {
            tripDaysBadge.textContent = `${dias} días de viaje`;
        }
    }

    // OPTIMIZADO: Función para validar etiquetas sin usar alert()
    function validarEtiquetas() {
        if (!etiquetasInput || !etiquetasError) return true;
        const etiquetas = etiquetasInput.value.trim();
        if (!etiquetas) {
            etiquetasError.classList.add('d-none'); // Ocultar si está vacío
            return true;
        }
        const etiquetasInvalidas = etiquetas.split(',').map(tag => tag.trim()).filter(tag => tag.length > 50);

        if (etiquetasInvalidas.length > 0) {
            etiquetasError.textContent = `Error: Las siguientes etiquetas superan los 50 caracteres: ${etiquetasInvalidas.join(', ')}`;
            etiquetasError.classList.remove('d-none'); // Mostrar el error
            return false;
        }
        etiquetasError.classList.add('d-none'); // Ocultar si todo es válido
        return true;
    }

    // OPTIMIZADO: Función para generar el itinerario usando el <template>
    function generarItinerario() {
        const dias = calcularDiasViaje();
        if (dias <= 0 || !itineraryTemplate) {
            if (noDatesWarning) noDatesWarning.classList.remove('d-none');
            if (itineraryAccordion) itineraryAccordion.innerHTML = '';
            return;
        }

        if (noDatesWarning) noDatesWarning.classList.add('d-none');
        if (itineraryAccordion) itineraryAccordion.innerHTML = '';

        for (let i = 1; i <= dias; i++) {
            const templateNode = itineraryTemplate.content.cloneNode(true);
            const button = templateNode.querySelector('.accordion-button');
            const collapseDiv = templateNode.querySelector('.accordion-collapse');

            // Personalizamos el clon con los datos correctos para cada día
            button.dataset.bsTarget = `#day${i}Collapse`;
            button.querySelector('span').textContent = `Día ${i}`;
            collapseDiv.id = `day${i}Collapse`;

            itineraryAccordion.appendChild(templateNode);
        }

        const primerDia = document.querySelector('#day1Collapse');
        if (primerDia) {
            new bootstrap.Collapse(primerDia, { show: true });
        }
    }


    // --- EVENT LISTENERS (mantenemos tus listeners vitales) ---

    if (generarItinerarioBtn) generarItinerarioBtn.addEventListener('click', generarItinerario);
    if (etiquetasInput) etiquetasInput.addEventListener('input', validarEtiquetas);

    // Tus listeners para las fechas y el badge, perfectos.
    if (fechaInicioInput) fechaInicioInput.addEventListener('change', actualizarDiasBadge);
    if (fechaFinInput) fechaFinInput.addEventListener('change', actualizarDiasBadge);

    // Tu lógica para validar rangos de edad y fechas mínimas/máximas. ¡Vital y se mantiene!
    if (rangoEdadMin && rangoEdadMax) {
        rangoEdadMin.addEventListener('change', function () {
            if (parseInt(this.value) > parseInt(rangoEdadMax.value)) {
                rangoEdadMax.value = this.value;
            }
        });
        rangoEdadMax.addEventListener('change', function () {
            if (parseInt(this.value) < parseInt(rangoEdadMin.value)) {
                rangoEdadMin.value = this.value;
            }
        });
    }
    if (fechaInicioInput && fechaFinInput) {
        fechaInicioInput.min = new Date().toISOString().split('T')[0];
        fechaInicioInput.addEventListener('change', () => {
            fechaFinInput.min = fechaInicioInput.value;
        });
    }

    // OPTIMIZADO: Validación del formulario al enviar (mucho más simple)
    form.addEventListener('submit', function (event) {
        // Ejecutamos nuestra validación personalizada de etiquetas
        const sonEtiquetasValidas = validarEtiquetas();

        // checkValidity() le pide al navegador que revise todos los 'required', 'min', 'max', etc.
        // Si el formulario del navegador NO es válido O nuestras etiquetas NO son válidas...
        if (!form.checkValidity() || !sonEtiquetasValidas) {
            event.preventDefault(); // ...detenemos el envío.
            event.stopPropagation();
        }

        // Esta clase es la que le dice a Bootstrap que muestre los estilos de error (bordes rojos, etc.)
        form.classList.add('was-validated');
    });
});
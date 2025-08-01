document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('grupoViajeForm');
    var nombreViajeInput = document.getElementById('nombreViaje');
    var destinoPrincipalInput = document.getElementById('destinoPrincipal');
    var puntoEncuentroInput = document.getElementById('puntoEncuentro');
    var imagenDestacadaInput = document.getElementById('imagenDestacada');
    var rangoEdadMin = document.getElementById('rangoEdadMin');
    var rangoEdadMax = document.getElementById('rangoEdadMax');
    var maxParticipantes = document.getElementById('maxParticipantes');
    var tipoGrupo = document.getElementById('tipoGrupo');
    var fechaInicio = document.getElementById('fechaInicio');
    var fechaFin = document.getElementById('fechaFin');
    var btnItinerario = document.getElementById('generarItinerarioBtn');
    var badgeDias = document.getElementById('trip-days-badge');
    var itineraryAccordion = document.getElementById('itineraryAccordion');
    var itineraryContainer = document.getElementById('itineraryContainer');
    var etiquetasInput = document.getElementById('etiquetasInput');
    var noDatesWarning = document.getElementById('no-dates-warning');

    function validarTexto(input) {
        if (!input) return true;
        var valor = input.value.trim();
        var valido = valor !== '';
        input.classList.toggle('is-invalid', !valido);
        return valido;
    }

    function validarNumero(input, min, max) {
        if (!input) return true;
        var valor = parseInt(input.value);
        var valido = !isNaN(valor) && valor >= min && valor <= max;
        input.classList.toggle('is-invalid', !valido);
        return valido;
    }

    function validarFechas() {
        if (!fechaInicio || !fechaFin) return true;
        var inicio = new Date(fechaInicio.value);
        var fin = new Date(fechaFin.value);
        var hoy = new Date();
        hoy.setHours(0, 0, 0, 0);
        var inicioValido = !isNaN(inicio.getTime()) && inicio >= hoy;
        var finValido = !isNaN(fin.getTime()) && fin >= inicio;
        fechaInicio.classList.toggle('is-invalid', !inicioValido);
        fechaFin.classList.toggle('is-invalid', !finValido);
        return inicioValido && finValido;
    }

    function calcularDias() {
        if (fechaInicio.value && fechaFin.value) {
            var inicio = new Date(fechaInicio.value);
            var fin = new Date(fechaFin.value);
            return Math.ceil((fin - inicio) / (1000 * 60 * 60 * 24)) + 1;
        }
        return 0;
    }

    function actualizarDiasBadge() {
        var dias = calcularDias();
        if (badgeDias) {
            badgeDias.textContent = dias + ' días de viaje';
        }
    }

    function validarEtiquetas(valor) {
        if (!valor.trim()) return true;
        var etiquetas = valor.split(',').map(function (e) { return e.trim(); });
        var invalidas = etiquetas.filter(function (e) { return e.length > 50; });
        if (invalidas.length > 0) {
            alert("Las siguientes etiquetas exceden los 50 caracteres:\n" + invalidas.join('\n'));
            return false;
        }
        return true;
    }

    function generarItinerario() {
        var dias = calcularDias();

        if (dias <= 0) {
            if (noDatesWarning) noDatesWarning.classList.remove('d-none');
            if (itineraryAccordion) itineraryAccordion.classList.add('d-none');
            return;
        }

        if (noDatesWarning) noDatesWarning.classList.add('d-none');
        if (itineraryAccordion) {
            itineraryAccordion.classList.remove('d-none');
            itineraryAccordion.innerHTML = '';
        }
        if (itineraryContainer) itineraryContainer.classList.remove('d-none');

        for (var i = 1; i <= dias; i++) {
            var item = document.createElement('div');
            item.className = 'accordion-item';
            item.innerHTML = `
        <h2 class="accordion-header my-3">
          <button class="accordion-button collapsed bg-secondary text-white p-2 rounded-2" type="button"
            data-bs-toggle="collapse" data-bs-target="#day${i}Collapse">
            <i class="bi bi-calendar-event me-2"></i>Día ${i}
          </button>
        </h2>
        <div id="day${i}Collapse" class="accordion-collapse collapse">
          <div class="accordion-body">
            <div class="mb-3">
              <label class="form-label">Título del día <span class="text-danger">*</span></label>
              <input type="text" name="itinerarioTitulo" class="form-control" required maxlength="100">
              <div class="invalid-feedback">El título es obligatorio y no puede superar los 100 caracteres.</div>
            </div>
            <div>
              <label class="form-label">Descripción del día (opcional)</label>
              <textarea name="itinerarioDescripcion" class="form-control" rows="2"></textarea>
            </div>
          </div>
        </div>`;
            itineraryAccordion.appendChild(item);
        }

        var primerDia = document.querySelector('#day1Collapse');
        if (primerDia) {
            new bootstrap.Collapse(primerDia, { show: true });
        }
        actualizarDiasBadge();
    }

    function validarRangoEdad() {
        var min = parseInt(rangoEdadMin.value);
        var max = parseInt(rangoEdadMax.value);
        if (!isNaN(min) && !isNaN(max)) {
            if (min > max) rangoEdadMax.value = min;
            else if (max < min) rangoEdadMin.value = max;
        }
    }

    // Listeners individuales
    if (nombreViajeInput) nombreViajeInput.addEventListener('input', function () { validarTexto(this); });
    if (destinoPrincipalInput) destinoPrincipalInput.addEventListener('input', function () { validarTexto(this); });
    if (puntoEncuentroInput) puntoEncuentroInput.addEventListener('input', function () { validarTexto(this); });
    if (imagenDestacadaInput) imagenDestacadaInput.addEventListener('input', function () { validarTexto(this); });

    if (rangoEdadMin) rangoEdadMin.addEventListener('input', function () { validarNumero(this, 18, 65); });
    if (rangoEdadMax) rangoEdadMax.addEventListener('input', function () { validarNumero(this, 18, 65); });

    if (rangoEdadMin && rangoEdadMax) {
        rangoEdadMin.addEventListener('change', validarRangoEdad);
        rangoEdadMax.addEventListener('change', validarRangoEdad);
    }

    if (maxParticipantes) maxParticipantes.addEventListener('change', function () {
        var val = parseInt(this.value);
        this.classList.toggle('is-invalid', isNaN(val) || val < 2);
    });

    if (tipoGrupo) tipoGrupo.addEventListener('change', function () {
        this.classList.toggle('is-invalid', !this.value);
    });

    if (fechaInicio) {
        fechaInicio.min = new Date().toISOString().split('T')[0];
        fechaInicio.addEventListener('change', function () {
            fechaFin.min = this.value;
            if (fechaFin.value && fechaFin.value < this.value) {
                fechaFin.value = this.value;
            }
            validarFechas();
            actualizarDiasBadge();
            if (!itineraryContainer.classList.contains('d-none')) {
                generarItinerario();
            }
        });
    }

    if (fechaFin) {
        fechaFin.addEventListener('change', function () {
            validarFechas();
            actualizarDiasBadge();
            if (!itineraryContainer.classList.contains('d-none')) {
                generarItinerario();
            }
        });
    }

    if (btnItinerario) {
        btnItinerario.addEventListener('click', function () {
            generarItinerario();
        });
    }

    if (etiquetasInput) {
        etiquetasInput.addEventListener('input', function () {
            var valido = validarEtiquetas(this.value);
            this.classList.toggle('is-invalid', !valido);
        });
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            var valido = true;

            valido = validarTexto(nombreViajeInput) && valido;
            valido = validarTexto(destinoPrincipalInput) && valido;
            valido = validarTexto(puntoEncuentroInput) && valido;
            valido = validarTexto(imagenDestacadaInput) && valido;
            valido = validarNumero(rangoEdadMin, 18, 65) && valido;
            valido = validarNumero(rangoEdadMax, 18, 65) && valido;
            valido = validarFechas() && valido;

            if (!maxParticipantes.value || parseInt(maxParticipantes.value) < 2) {
                maxParticipantes.classList.add('is-invalid');
                valido = false;
            }

            if (!tipoGrupo.value) {
                tipoGrupo.classList.add('is-invalid');
                valido = false;
            }

            var dias = calcularDias();
            if (dias > 0 && itineraryAccordion.children.length === 0) {
                alert('Por favor, genera el itinerario antes de enviar el formulario.');
                e.preventDefault();
                valido = false;
            }

            var titulos = itineraryAccordion.querySelectorAll('input[name="itinerarioTitulo"]');
            for (var i = 0; i < titulos.length; i++) {
                var titulo = titulos[i];
                if (!titulo.value.trim() || titulo.value.length > 100) {
                    titulo.classList.add('is-invalid');
                    valido = false;
                }
            }

            if (etiquetasInput && etiquetasInput.value.trim() !== '') {
                if (!validarEtiquetas(etiquetasInput.value)) {
                    etiquetasInput.classList.add('is-invalid');
                    valido = false;
                }
            }

            if (!form.checkValidity() || !valido) {
                e.preventDefault();
                e.stopPropagation();
            }

            form.classList.add('was-validated');
        });
    }
});

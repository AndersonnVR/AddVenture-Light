document.addEventListener("DOMContentLoaded", function () {
    // Seleccionar los elementos del DOM
    const fechaInicioInput = document.getElementById("fechaInicio");
    const fechaFinInput = document.getElementById("fechaFin");
    const tripDaysBadge = document.getElementById("trip-days-badge");
    const generarItinerarioBtn = document.getElementById("generarItinerarioBtn");
    const itineraryContainer = document.getElementById("itineraryContainer");
    const itineraryAccordion = document.getElementById("itineraryAccordion");
    const noDatesWarning = document.getElementById("no-dates-warning");
    const etiquetasInput = document.getElementById("etiquetasInput");
    const nombreViajeInput = document.getElementById("nombreViaje");
    const destinoPrincipalInput = document.getElementById("destinoPrincipal");
    const puntoEncuentroInput = document.getElementById("puntoEncuentro");
    const imagenDestacadaInput = document.getElementById("imagenDestacada");
    const linkGrupoWhatsappInput = document.getElementById("linkGrupoWhatsapp");
    const rangoEdadMin = document.getElementById("rangoEdadMin");
    const rangoEdadMax = document.getElementById("rangoEdadMax");
    const maxParticipantesSelect = document.getElementById("maxParticipantes");
    const tipoGrupoSelect = document.getElementById("tipoGrupo");

    // Función para validar campos de texto requeridos
    function validarCampoTexto(input) {
        // Si el input no existe, retornar true
        if (!input) return true;
        const valor = input.value.trim();
        const esValido = valor !== "";
        // Agregar la clase is-invalid si el valor no es válido
        input.classList.toggle("is-invalid", !esValido);
        return esValido;
    }

    // Función para validar campos numéricos
    function validarCampoNumerico(input, min, max) {
        // Si el input no existe, retornar true
        if (!input) return true;
        const valor = parseInt(input.value);
        // Evaluar si el valor es un número, si es mayor o igual a min y si es menor o igual a max
        const esValido = !isNaN(valor) && valor >= min && valor <= max;
        // Agregar la clase is-invalid si el valor no es válido
        input.classList.toggle("is-invalid", !esValido);
        return esValido;
    }

    // Función para validar el link de WhatsApp
    function validarLinkWhatsapp(input) {
        // Si el input no existe, retornar true
        if (!input) return true;
        const valor = input.value.trim();
        const regex = /^https:\/\/chat\.whatsapp\.com\/[A-Za-z0-9]{22}\/?$/;
        // Evaluar si el valor corresponde a un link válido
        const esValido = regex.test(valor);
        // Agregar la clase is-invalid si el valor no es válido
        input.classList.toggle("is-invalid", !esValido);
        return esValido;
    }

    // Función para eliminar posibles desfaces horarios
    function parseFechaSinHora(fechaStr) {
        const [anio, mes, dia] = fechaStr.split("-").map(Number);
        return new Date(anio, mes - 1, dia);
    }

    // Función para validar fechas
    function validarFechas() {
        // Si el input de fecha de inicio o fecha de fin no existe, retornar true
        if (!fechaInicioInput || !fechaFinInput) return true;
        const fechaInicio = parseFechaSinHora(fechaInicioInput.value);
        const fechaFin = parseFechaSinHora(fechaFinInput.value);
        // Obtener la fecha actual y ajustarla para mañana
        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);
        hoy.setDate(hoy.getDate() + 1);
        // Evaluar si las fechas de inicio y fin son válidas
        const fechaInicioValida =
            !isNaN(fechaInicio.getTime()) && fechaInicio >= hoy;
        const fechaFinValida =
            !isNaN(fechaFin.getTime()) && fechaFin >= fechaInicio;
        // Agregar la clase is-invalid si la fecha de inicio o fin no es válida
        fechaInicioInput.classList.toggle("is-invalid", !fechaInicioValida);
        fechaFinInput.classList.toggle("is-invalid", !fechaFinValida);
        return fechaInicioValida && fechaFinValida;
    }

    // Función para calcular días entre fechas
    function calcularDiasViaje() {
        if (fechaInicioInput.value && fechaFinInput.value) {
            const inicio = new Date(fechaInicioInput.value);
            const fin = new Date(fechaFinInput.value);
            // Calcular la diferencia entre las fechas y sumar 1 para incluir el día de inicio
            const diferencia = Math.ceil((fin - inicio) / (1000 * 60 * 60 * 24)) + 1;
            return diferencia > 0 ? diferencia : 0;
        }
        // Devolver 0 si no hay fechas seleccionadas
        return 0;
    }

    // Función para actualizar el badge de días de viaje
    function actualizarDiasBadge() {
        const dias = calcularDiasViaje();
        // Si el elemento tripDaysBadge existe
        if (tripDaysBadge) {
            // Actualizar el texto del badge con la cantidad de días de viaje
            if (dias === 1) {
                tripDaysBadge.textContent = `${dias} día de viaje`;
            } else {
                tripDaysBadge.textContent = `${dias} días de viaje`;
            }
        }
    }

    // Función para validar etiquetas
    function validarEtiquetas(etiquetas) {
        // Si el valor de las etiquetas está vacío, retornar true
        if (!etiquetas.trim()) return true;
        // Convertir el valor de las etiquetas a un array de etiquetas
        const etiquetasArray = etiquetas.split(",").map((tag) => tag.trim());
        // Filtrar las etiquetas que exceden los 50 caracteres
        const etiquetasInvalidas = etiquetasArray.filter((tag) => tag.length > 50);
        if (etiquetasInvalidas.length > 0) {
            const mensaje = `Las siguientes etiquetas exceden los 50 caracteres:\n${etiquetasInvalidas.join(
                "\n"
            )}`;
            alert(mensaje);
            return false;
        }
        // Devolver true si las etiquetas son válidas, false si no
        return true;
    }

    // Función para generar el itinerario
    function generarItinerario() {
        const dias = calcularDiasViaje();

        // Si el número de días de viaje es menor o igual a 0, mostrar un mensaje de alerta y ocultar el acordeón de itinerario
        if (dias <= 0) {
            if (noDatesWarning) noDatesWarning.classList.remove("d-none");
            if (itineraryAccordion) itineraryAccordion.classList.add("d-none");
            return;
        }

        // Si el elemento noDatesWarning existe, ocultar el mensaje de alerta
        if (noDatesWarning) noDatesWarning.classList.add("d-none");
        // Si el elemento itineraryAccordion existe, mostrar el acordeón de itinerario
        if (itineraryAccordion) {
            itineraryAccordion.classList.remove("d-none");
            itineraryAccordion.innerHTML = "";
        }

        // Si el elemento itineraryContainer existe, mostrar el contenedor de itinerario
        if (itineraryContainer) itineraryContainer.classList.remove("d-none");

        const diaHTML = (i) => `
        <h2 class="accordion-header my-3">
          <button class="accordion-button collapsed bg-secondary text-white p-2 rounded-2" type="button" data-bs-toggle="collapse"
            data-bs-target="#day${i}Collapse">
            <i class="bi bi-calendar-event me-2"></i>
            Día ${i + 1}
          </button>
        </h2>
        <div id="day${i}Collapse" class="accordion-collapse collapse">
          <div class="accordion-body">
            <div class="mb-3">
              <label class="form-label">
                Título del día
                <span class="text-danger">*</span>
              </label>
              <input type="text" class="form-control titulo-itinerario" name="itinerarios[${i}].titulo" placeholder="Ej: Llegada y check-in" required maxlength="100">
              <div class="invalid-feedback">El título es obligatorio y no puede superar los 100 caracteres.</div>
            </div>
            <div>
              <label class="form-label">Descripción del día (opcional)</label>
              <textarea class="form-control" name="itinerarios[${i}].descripcion" rows="2"
                placeholder="Describe las actividades planificadas para este día"></textarea>
            </div>
          </div>
        </div>
      `;

        // Generar un acordeón para cada día
        for (let i = 0; i < dias; i++) {
            const acordeonItem = document.createElement("div");
            acordeonItem.className = "accordion-item";
            acordeonItem.innerHTML = diaHTML(i);

            const tituloInput = acordeonItem.querySelector(".titulo-itinerario");
            if (tituloInput) {
                tituloInput.addEventListener("input", function () {
                    // Eliminar la clase is-invalid
                    this.classList.remove("is-invalid");
                    // Evaluar si el valor del campo está vacío
                    if (this.value.trim() === "") {
                        this.classList.add("is-invalid");
                    } else if (this.value.length > 100) {
                        // Si el valor del campo excede los 100 caracteres, cortar el valor a 100 caracteres
                        this.value = this.value.substring(0, 100);
                    }
                });
            }

            if (itineraryAccordion) {
                itineraryAccordion.appendChild(acordeonItem);
            }
        }

        // Abrir el primer día automáticamente
        const primerDia = document.querySelector("#day0Collapse");
        if (primerDia) {
            new bootstrap.Collapse(primerDia, { show: true });
        }
    }

    // Event Listeners para validación en tiempo real
    [
        nombreViajeInput,
        destinoPrincipalInput,
        puntoEncuentroInput,
        imagenDestacadaInput,
    ].forEach((input) => {
        if (input) {
            input.addEventListener("input", () => validarCampoTexto(input));
        }
    });

    [rangoEdadMin, rangoEdadMax].forEach((input) => {
        if (input) {
            input.addEventListener("input", () =>
                validarCampoNumerico(input, 18, 65)
            );
        }
    });

    if (linkGrupoWhatsappInput) {
        linkGrupoWhatsappInput.addEventListener("input", () => {
            validarLinkWhatsapp(linkGrupoWhatsappInput);
        });
    }

    if (maxParticipantesSelect) {
        maxParticipantesSelect.addEventListener("change", () => {
            const valor = parseInt(maxParticipantesSelect.value);
            maxParticipantesSelect.classList.toggle(
                "is-invalid",
                isNaN(valor) || valor < 2
            );
        });
    }

    if (tipoGrupoSelect) {
        tipoGrupoSelect.addEventListener("change", () => {
            tipoGrupoSelect.classList.toggle("is-invalid", !tipoGrupoSelect.value);
        });
    }

    // Event Listeners para fechas
    if (fechaInicioInput) {
        fechaInicioInput.addEventListener("change", () => {
            validarFechas();
            actualizarDiasBadge();
            if (
                itineraryContainer &&
                !itineraryContainer.classList.contains("d-none")
            ) {
                generarItinerario();
            }
        });
    }

    if (fechaFinInput) {
        fechaFinInput.addEventListener("change", () => {
            validarFechas();
            actualizarDiasBadge();
            if (
                itineraryContainer &&
                !itineraryContainer.classList.contains("d-none")
            ) {
                generarItinerario();
            }
        });
    }

    if (generarItinerarioBtn) {
        generarItinerarioBtn.addEventListener("click", generarItinerario);
    }

    // Validación de etiquetas
    if (etiquetasInput) {
        etiquetasInput.addEventListener("input", function () {
            const isValid = validarEtiquetas(this.value);
            this.classList.toggle("is-invalid", !isValid);
        });
    }

    // Validación del formulario
    const form = document.getElementById("grupoViajeForm");
    if (form) {
        form.addEventListener("submit", function (event) {
            let isValid = true;

            // Validar campos de texto requeridos
            isValid = validarCampoTexto(nombreViajeInput) && isValid;
            isValid = validarCampoTexto(destinoPrincipalInput) && isValid;
            isValid = validarCampoTexto(puntoEncuentroInput) && isValid;
            isValid = validarCampoTexto(imagenDestacadaInput) && isValid;

            // Validar campos numéricos
            isValid = validarCampoNumerico(rangoEdadMin, 18, 65) && isValid;
            isValid = validarCampoNumerico(rangoEdadMax, 18, 65) && isValid;

            // Validar link de WhatsApp
            isValid = validarLinkWhatsapp(linkGrupoWhatsappInput) && isValid;

            // Validar selects
            if (maxParticipantesSelect) {
                const valor = parseInt(maxParticipantesSelect.value);
                if (isNaN(valor) || valor < 2) {
                    maxParticipantesSelect.classList.add("is-invalid");
                    isValid = false;
                }
            }

            if (tipoGrupoSelect && !tipoGrupoSelect.value) {
                tipoGrupoSelect.classList.add("is-invalid");
                isValid = false;
            }

            // Validar fechas
            isValid = validarFechas() && isValid;

            // Validar que haya un itinerario si las fechas están seleccionadas
            const dias = calcularDiasViaje();
            if (
                dias > 0 &&
                itineraryAccordion &&
                itineraryAccordion.children.length === 0
            ) {
                event.preventDefault();
                alert("Por favor, genera el itinerario antes de enviar el formulario.");
                isValid = false;
            }

            // Validar solo los títulos del itinerario (requeridos)
            if (
                itineraryAccordion &&
                !itineraryAccordion.classList.contains("d-none")
            ) {
                const titulosInputs = itineraryAccordion.querySelectorAll(
                    ".titulo-itinerario"
                );
                titulosInputs.forEach((input) => {
                    if (!input.value.trim() || input.value.length > 100) {
                        input.classList.add("is-invalid");
                        isValid = false;
                    }
                });
            }

            // Validar etiquetas si se han ingresado
            if (etiquetasInput && etiquetasInput.value.trim() !== "") {
                if (!validarEtiquetas(etiquetasInput.value)) {
                    etiquetasInput.classList.add("is-invalid");
                    isValid = false;
                }
            }

            if (!form.checkValidity() || !isValid) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add("was-validated");
        });
    }

    // Validación de rango de edades
    if (rangoEdadMin && rangoEdadMax) {
        rangoEdadMin.addEventListener("change", function () {
            const minVal = parseInt(this.value);
            const maxVal = parseInt(rangoEdadMax.value);
            if (!isNaN(minVal) && !isNaN(maxVal) && minVal > maxVal) {
                rangoEdadMax.value = this.value;
            }
            validarCampoNumerico(this, 18, 65);
        });

        rangoEdadMax.addEventListener("change", function () {
            const minVal = parseInt(rangoEdadMin.value);
            const maxVal = parseInt(this.value);
            if (!isNaN(minVal) && !isNaN(maxVal) && maxVal < minVal) {
                rangoEdadMin.value = this.value;
            }
            validarCampoNumerico(this, 18, 65);
        });
    }

    // Validación de fechas
    if (fechaInicioInput && fechaFinInput) {
        const mañana = new Date();
        mañana.setDate(mañana.getDate() + 1);
        const mañanaISOString = mañana.toISOString().split("T")[0];

        fechaInicioInput.min = mañanaISOString;

        fechaInicioInput.addEventListener("change", function () {
            fechaFinInput.min = this.value;
            if (fechaFinInput.value && fechaFinInput.value < this.value) {
                fechaFinInput.value = this.value;
            }
            validarFechas();
        });

        if (fechaFinInput.value) {
            fechaInicioInput.max = fechaFinInput.value;
        }
    }
});

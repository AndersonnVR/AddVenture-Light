document.addEventListener('DOMContentLoaded', function () {
    // Seleccionar los elementos del DOM
    const itineraryAccordion = document.getElementById("itineraryAccordion");
    const nombreViajeInput = document.getElementById("nombreViaje");
    const destinoPrincipalInput = document.getElementById("destinoPrincipal");
    const rangoEdadMin = document.getElementById("rangoEdadMin");
    const rangoEdadMax = document.getElementById("rangoEdadMax");
    const tipoGrupoSelect = document.getElementById("tipoGrupo");
    const maxParticipantesSelect = document.getElementById("maxParticipantes");
    const puntoEncuentroInput = document.getElementById("puntoEncuentro");
    const imagenDestacadaInput = document.getElementById("imagenDestacada");
    const linkGrupoWhatsappInput = document.getElementById("linkGrupoWhatsapp")

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

    // Función para validar títulos de itinerario
    function validarTitulosItinerario() {
        const tituloInputs = document.querySelectorAll(".titulo-itinerario");
        tituloInputs.forEach((input) => {
            input.addEventListener("input", function () {
                this.classList.remove("is-invalid");
                const valor = this.value.trim();
                if (valor === "") {
                    this.classList.add("is-invalid");
                } else if (valor.length > 100) {
                    this.value = valor.substring(0, 100);
                }
            });
        });
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

    validarTitulosItinerario();

    // Validación del formulario
    const form = document.getElementById("editarGrupoForm");
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

            // Validar solo los títulos del itinerario (requeridos)
            if (
                itineraryAccordion &&
                !itineraryAccordion.classList.contains("d-none")
            ) {
                const titulosInputs = itineraryAccordion.querySelectorAll(
                    'input[name="itinerarioTitulo"]'
                );
                titulosInputs.forEach((input) => {
                    if (!input.value.trim() || input.value.length > 100) {
                        input.classList.add("is-invalid");
                        isValid = false;
                    }
                });
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

});

function togglePassword(inputId, button) {
    const input = document.getElementById(inputId);
    const icon = button.querySelector("i");

    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("bi-eye", "bi-eye-slash");
    } else {
        input.type = "password";
        icon.classList.replace("bi-eye-slash", "bi-eye");
    }
<<<<<<< Updated upstream
}
=======
}

document.addEventListener('DOMContentLoaded', function() {
    const datosCiudades = {
        "Perú": [
            // 24 Departamentos + 1 Provincia Constitucional
            "Amazonas", "Áncash", "Apurímac", "Arequipa", "Ayacucho",
            "Cajamarca", "Callao", "Cusco", "Huancavelica", "Huánuco",
            "Ica", "Junín", "La Libertad", "Lambayeque", "Lima",
            "Loreto", "Madre de Dios", "Moquegua", "Pasco", "Piura",
            "Puno", "San Martín", "Tacna", "Tumbes", "Ucayali"
        ],
        "Colombia": [
            // 32 Departamentos + 1 Distrito Capital
            "Amazonas", "Antioquia", "Arauca", "Atlántico", "Bogotá D.C.",
            "Bolívar", "Boyacá", "Caldas", "Caquetá", "Casanare",
            "Cauca", "Cesar", "Chocó", "Córdoba", "Cundinamarca",
            "Guainía", "Guaviare", "Huila", "La Guajira", "Magdalena",
            "Meta", "Nariño", "Norte de Santander", "Putumayo", "Quindío",
            "Risaralda", "San Andrés y Providencia", "Santander", "Sucre",
            "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
        ],
        "Argentina": [
            // 23 Provincias + 1 Ciudad Autónoma
            "Buenos Aires", "Catamarca", "Chaco", "Chubut",
            "Ciudad Autónoma de Buenos Aires", "Córdoba", "Corrientes",
            "Entre Ríos", "Formosa", "Jujuy", "La Pampa", "La Rioja",
            "Mendoza", "Misiones", "Neuquén", "Río Negro", "Salta",
            "San Juan", "San Luis", "Santa Cruz", "Santa Fe",
            "Santiago del Estero", "Tierra del Fuego", "Tucumán"
        ],
        "Brasil": [
            // 26 Estados + 1 Distrito Federal
            "Acre", "Alagoas", "Amapá", "Amazonas", "Bahia", "Ceará",
            "Distrito Federal", "Espírito Santo", "Goiás", "Maranhão",
            "Mato Grosso", "Mato Grosso do Sul", "Minas Gerais", "Pará",
            "Paraíba", "Paraná", "Pernambuco", "Piauí", "Rio de Janeiro",
            "Rio Grande do Norte", "Rio Grande do Sul", "Rondônia",
            "Roraima", "Santa Catarina", "São Paulo", "Sergipe", "Tocantins"
        ]
    };

    const selectPais = document.getElementById('selectPais');
    const selectCiudad = document.getElementById('selectCiudad');

    selectPais.addEventListener('change', function() {
        const paisSeleccionado = this.value;
        
        // Limpiamos las opciones anteriores de ciudad
        selectCiudad.innerHTML = '<option value="">Selecciona tu ciudad...</option>';

        if (paisSeleccionado && datosCiudades[paisSeleccionado]) {
            // Si hay país válido, habilitamos el select de ciudad
            selectCiudad.disabled = false;

            // Recorremos las ciudades y creamos los <option>
            datosCiudades[paisSeleccionado].forEach(ciudad => {
                const option = document.createElement('option');
                option.value = ciudad;
                option.textContent = ciudad;
                selectCiudad.appendChild(option);
            });
        } else {
            // Si no hay país, deshabilitamos ciudad
            selectCiudad.disabled = true;
            selectCiudad.innerHTML = '<option value="">Selecciona primero un país</option>';
        }
    });
    
    if (selectPais.value) {
        selectPais.dispatchEvent(new Event('change'));
    }

});
>>>>>>> Stashed changes

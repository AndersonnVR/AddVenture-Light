document.getElementById("botonVerificar").addEventListener("click", function (event) {
  event.preventDefault(); // ⚠️ Prevenir el submit del form

  const codigo = document.getElementById("codigo").value.trim();
  const email = localStorage.getItem("email");

  // Validación básica
  if (!codigo) {
    mostrarError("Por favor ingresa el código");
    return;
  }

  if (!email) {
    mostrarError("No se encontró el email. Por favor regresa al login.");
    return;
  }

  // Deshabilitar botón para evitar múltiples clicks
  const boton = document.getElementById("botonVerificar");
  boton.disabled = true;
  boton.textContent = "Verificando...";

  fetch("/api/auth/verify-code", {
    method: "POST",
    headers: { 
      "Content-Type": "application/json" 
    },
    credentials: "same-origin", // ✅ CRÍTICO: Envía cookies de sesión
    body: JSON.stringify({
      email: email,
      code: codigo
    })
  })
    .then(response => response.json())
    .then(data => {
      if (data.verified) {
        // ✅ Código válido - redirigir al login
        localStorage.removeItem("email"); // Limpiar email del localStorage
        window.location.href = data.redirect || "/login";
      } else {
        // ❌ Código inválido
        mostrarError(data.message || "Código inválido o expirado");
        boton.disabled = false;
        boton.textContent = "Verificar";
      }
    })
    .catch(error => {
      console.error("Error en la petición:", error);
      mostrarError("No se pudo conectar con el servidor. Intenta de nuevo.");
      boton.disabled = false;
      boton.textContent = "Verificar";
    });
});

// Función helper para mostrar errores
function mostrarError(mensaje) {
  const mensajeError = document.getElementById("mensajeError");
  if (mensajeError) {
    mensajeError.textContent = mensaje;
    mensajeError.style.display = "block";
    
    // Ocultar después de 5 segundos
    setTimeout(() => {
      mensajeError.style.display = "none";
    }, 5000);
  } else {
    alert(mensaje);
  }
}

// Permitir verificar con Enter
document.getElementById("codigo").addEventListener("keypress", function(event) {
  if (event.key === "Enter") {
    event.preventDefault();
    document.getElementById("botonVerificar").click();
  }
});
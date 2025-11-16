/*
document
  .getElementById("botonVerificar")
  .addEventListener("click", function () {
    fetch("http://127.0.0.1:8030/api/auth/verify-code", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: sessionStorage.getItem("email"),
        code: document.getElementById("codigo").value,
      }),
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.verified) {
          sessionStorage.clear();
          window.location.href = data.redirect;
        } else {
          alert("Error: " + data.message);
          event.preventDefault();
          // evita que el navegador cambie de página
        }
      })
      .catch((error) => {
        console.error("Error en la petición:", error);
        alert("No se pudo conectar con el servidor.");
        event.preventDefault();
      });
  });

  */

  document.getElementById("botonVerificar").addEventListener("click", function () {
  fetch("http://127.0.0.1:8030/api/auth/verify-code", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email: localStorage.getItem("email"),
      code: document.getElementById("codigo").value
    }),
  })
  .then(async response => {
    const text = await response.text();

    try {
      const json = JSON.parse(text);

      if (!response.ok) {
        alert("Error: " + (json.message || "Verificación fallida"));
      } else {
        // ✅ Redirige al perfil
        localStorage.clear();
        window.location.href = "/login";
      }
    } catch (e) {
      console.error("Respuesta no es JSON:", text);
      alert("Error inesperado del servidor.");
    }
  })
  .catch(error => {
    console.error("Error en la petición:", error);
    alert("No se pudo conectar con el servidor.");
  });
});


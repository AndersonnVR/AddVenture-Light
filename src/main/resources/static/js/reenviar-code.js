document.getElementById("reenviarCodigo").addEventListener("click", function(event) {
  event.preventDefault(); // evita que el navegador cambie de página


    const email = localStorage.getItem("email");

  // Aquí puedes llamar a tu API si quieres reenviar el código
  fetch("http://127.0.0.1:8030/api/auth/send-code", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: email })
  })
  .then(response => response.json())
  .then(data => {
    // Actualiza el mensaje en la misma página
    document.getElementById("codigoHelp").innerHTML = "Código reenviado. Revisa tu bandeja.";
  })
  .catch(error => {
    console.error("Error al reenviar:", error);
    document.getElementById("codigoHelp").innerHTML = "No se pudo reenviar el código.";
  });
});

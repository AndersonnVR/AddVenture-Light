document.getElementById("registroBoton").addEventListener("click", function () {
  const email = document.getElementById("emailInput").value;

  localStorage.setItem("email", email);

  fetch("http://127.0.0.1:8030/api/auth/send-code", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email: localStorage.getItem("email")
    }),
  })
    .then(async (response) => {
      const text = await response.text(); // leer la respuesta como texto

      try {
        return JSON.parse(text); // intentar convertir a JSON
      } catch (e) {
        console.error("El backend NO devolvió JSON. Respuesta recibida:");
        console.log(text); // mostrar el HTML o error que envía Spring
        throw new Error("El backend no devolvió JSON");
      }
    })
    .then((data) => {
      console.log("Respuesta del backend (JSON):", data);
    })
    .catch((error) => {
      console.error("Error en el POST:", error);
    });
});

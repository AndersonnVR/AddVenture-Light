// Destinations data
const destinations = [
    {
      id: 1,
      name: "Cusco",
      country: "Perú",
      image: "../img/machu-picchu.jpg",
      dates: "Julio - Agosto",
      tag: "AVENTURA",
      tagColor: "bg-success text-white",
    },
    {
      id: 2,
      name: "Buenos Aires",
      country: "Argentina",
      image: "../img/buenos-aires.jpg",
      dates: "Septiembre - Octubre",
      tag: "CULTURAL",
      tagColor: "bg-warning text-dark",
    },
    {
      id: 3,
      name: "Cancún",
      country: "México",
      image: "../img/cancun.webp",
      dates: "Febrero - Abril",
      tag: "PLAYA",
      tagColor: "bg-info text-white",
    },
    {
      id: 4,
      name: "Cartagena",
      country: "Colombia",
      image: "../img/cartagena.jpeg",
      dates: "Noviembre - Diciembre",
      tag: "GASTRONÓMICO",
      tagColor: "bg-danger text-white",
    },
    {
      id: 5,
      name: "Santiago",
      country: "Chile",
      image: "../img/santiago.jpg",
      dates: "Octubre - Noviembre",
      tag: "URBANO",
      tagColor: "bg-primary text-white",
    },
    {
      id: 6,
      name: "Galápagos",
      country: "Ecuador",
      image: "../img/galapagos.jpeg",
      dates: "Mayo - Junio",
      tag: "NATURALEZA",
      tagColor: "bg-success text-white",
    },
  ];
  
  // Scroll animation
  document.addEventListener("DOMContentLoaded", function () {
    // Fade-in animation
    const fadeElements = document.querySelectorAll(".fade-in");
  
    const fadeInObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const delay = entry.target.getAttribute("data-delay") || 0;
            setTimeout(() => {
              entry.target.classList.add("active");
            }, delay);
          }
        });
      },
      { threshold: 0.1 }
    );
  
    fadeElements.forEach((element) => {
      fadeInObserver.observe(element);
    });
  
    // Initialize destination carousel
    initDestinationCarousel();
  
    // Newsletter form submission
    const newsletterForm = document.getElementById("newsletter-form");
    if (newsletterForm) {
      newsletterForm.addEventListener("submit", function (e) {
        e.preventDefault();
        alert("¡Gracias por suscribirte a nuestro boletín de noticias!");
        this.reset();
      });
    }
  });
  
  // Initialize destination carousel
  function initDestinationCarousel() {
    const carouselContainer = document.querySelector(".carousel-container");
    const indicatorsContainer = document.querySelector(".carousel-indicators");
    const prevButton = document.querySelector(".carousel-control.prev");
    const nextButton = document.querySelector(".carousel-control.next");
  
    let currentSlide = 0;
    const slidesPerView = window.innerWidth < 768 ? 1 : 4;
    const totalSlides = Math.ceil(destinations.length / slidesPerView);
  
    // Create destination cards
    destinations.forEach((destination) => {
      const card = document.createElement("div");
      card.className = "col-12 col-md-3 px-2";
      card.style.minWidth = window.innerWidth < 768 ? "100%" : "25%";
  
      card.innerHTML = `
            <div class="destination-card">
              <div class="destination-img" style="background-image: url('${destination.image}')">
                <span class="destination-country">
                  <i class="fas fa-map-marker-alt me-1"></i> ${destination.country}
                </span>
                <span class="destination-tag ${destination.tagColor}">${destination.tag}</span>
              </div>
              <div class="p-3">
                <h3 class="h5 fw-bold mb-1">${destination.name}</h3>
                <p class="small text-muted mb-3">${destination.dates}</p>
                <a href="#" class="btn btn-outline-primary btn-sm w-100">Ver grupos</a>
              </div>
            </div>
          `;
  
      carouselContainer.appendChild(card);
    });
  
    // Create indicators
    for (let i = 0; i < totalSlides; i++) {
      const indicator = document.createElement("div");
      indicator.className = `carousel-indicator ${i === 0 ? "active" : ""}`;
      indicator.addEventListener("click", () => goToSlide(i));
      indicatorsContainer.appendChild(indicator);
    }
  
    // Add event listeners to controls
    prevButton.addEventListener("click", () => {
      currentSlide = currentSlide === 0 ? totalSlides - 1 : currentSlide - 1;
      updateCarousel();
    });
  
    nextButton.addEventListener("click", () => {
      currentSlide = currentSlide === totalSlides - 1 ? 0 : currentSlide + 1;
      updateCarousel();
    });
  
    // Auto slide
    setInterval(() => {
      currentSlide = currentSlide === totalSlides - 1 ? 0 : currentSlide + 1;
      updateCarousel();
    }, 5000);
  
    // Go to specific slide
    function goToSlide(index) {
      currentSlide = index;
      updateCarousel();
    }
  
    // Update carousel position
    function updateCarousel() {
      const slideWidth = 100 / slidesPerView;
      carouselContainer.style.transform = `translateX(-${
        currentSlide * slideWidth * slidesPerView
      }%)`;
  
      // Update indicators
      document
        .querySelectorAll(".carousel-indicator")
        .forEach((indicator, index) => {
          indicator.classList.toggle("active", index === currentSlide);
        });
    }
  
    // Initial update
    updateCarousel();
  
    // Handle window resize
    window.addEventListener("resize", () => {
      const newSlidesPerView = window.innerWidth < 768 ? 1 : 4;
      if (newSlidesPerView !== slidesPerView) {
        location.reload();
      }
    });
  }
  
// Fechar menu ao clicar em um link
document.querySelectorAll('.nav-btn').forEach(function(btn) {
  btn.addEventListener('click', function() {
    document.getElementById('nav-buttons').classList.remove('active');
  });
});

// Scroll suave para links internos
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function (e) {
    e.preventDefault();
    document.querySelector(this.getAttribute('href')).scrollIntoView({
      behavior: 'smooth'
    });
  });
});
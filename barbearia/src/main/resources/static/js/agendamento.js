
// Inicialização do Flatpickr para seleção de data e hora
flatpickr("#dataHora", {
  enableTime: true,
  dateFormat: "Y-m-d H:i",
  minDate: "today",
  time_24hr: true,
  minuteIncrement: 30,
  disable: [function(date) {
    // Desativa domingos (0 = domingo, 6 = sábado)
    return date.getDay() === 0;
  }],
  locale: {
    firstDayOfWeek: 1, // começa a semana na segunda-feira
    weekdays: {
      shorthand: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'],
      longhand: ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado']
    },
   
  },
  disableMobile: "true",
  theme: "dark"
});

// Validação do formulário
document.getElementById('appointment-form').addEventListener('submit', function(e) {
  let valid = true;
  
  // Validar serviço
  const servico = document.getElementById('servico');
  if (servico.value === '') {
    document.getElementById('service-group').classList.add('error');
    valid = false;
  } else {
    document.getElementById('service-group').classList.remove('error');
  }
  
  // Validar barbeiro
  const barbeiro = document.getElementById('barbeiro');
  if (barbeiro.value === '') {
    document.getElementById('barber-group').classList.add('error');
    valid = false;
  } else {
    document.getElementById('barber-group').classList.remove('error');
  }
  
  // Validar data e hora
  const dataHora = document.getElementById('dataHora');
  if (dataHora.value === '') {
    document.getElementById('datetime-group').classList.add('error');
    valid = false;
  } else {
    document.getElementById('datetime-group').classList.remove('error');
  }
  
  if (!valid) {
    e.preventDefault();
  }
});

// Adiciona validação durante a digitação/seleção
document.getElementById('servico').addEventListener('change', function() {
  if (this.value !== '') {
    document.getElementById('service-group').classList.remove('error');
  }
});

document.getElementById('barbeiro').addEventListener('change', function() {
  if (this.value !== '') {
    document.getElementById('barber-group').classList.remove('error');
  }
});

document.getElementById('dataHora').addEventListener('change', function() {
  if (this.value !== '') {
    document.getElementById('datetime-group').classList.remove('error');
  }
});
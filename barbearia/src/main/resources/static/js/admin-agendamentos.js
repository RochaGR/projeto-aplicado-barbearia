document.addEventListener('DOMContentLoaded', function() {
    // Função de filtro
    window.filtrarAgendamentos = function() {
        const data = document.getElementById('dataFiltro').value;
        const status = document.getElementById('statusFiltro').value;

        let url = '/admin/agendamentos?';
        if (data) url += `data=${data}&`;
        if (status) url += `status=${status}`;
        
        // Remove o & final se existir
        if (url.endsWith('&')) {
            url = url.slice(0, -1);
        }
        
        window.location.href = url;
    };
    

    
});
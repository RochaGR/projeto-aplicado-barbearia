
    function abrirModal() {
    document.getElementById("modalTermos").style.display = "flex";
}
    function fecharModal() {
    document.getElementById("modalTermos").style.display = "none";
}
    window.onclick = function(event) {
    const modal = document.getElementById("modalTermos");
    if (event.target === modal) {
    modal.style.display = "none";
}
}

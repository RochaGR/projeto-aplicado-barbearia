package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do ServicoService")
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servico;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte de Cabelo");
        servico.setPreco(50.0);
        servico.setDuracaoMinutos(30);
        servico.setAtivo(true);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        Optional<Servico> resultado = servicoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Corte de Cabelo", resultado.get().getNome());
        verify(servicoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar serviço por ID inexistente")
    void deveRetornarVazioAoBuscarServicoPorIdInexistente() {
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Servico> resultado = servicoService.buscarPorId(999L);

        assertFalse(resultado.isPresent());
        verify(servicoRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve cadastrar novo serviço com sucesso")
    void deveCadastrarNovoServicoComSucesso() {
        when(servicoRepository.existsByNome("Corte de Cabelo")).thenReturn(false);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        Servico resultado = servicoService.cadastrar(servico);

        assertNotNull(resultado);
        assertEquals("Corte de Cabelo", resultado.getNome());
        verify(servicoRepository).existsByNome("Corte de Cabelo");
        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar serviço com nome duplicado")
    void deveLancarExcecaoAoCadastrarServicoComNomeDuplicado() {
        when(servicoRepository.existsByNome("Corte de Cabelo")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> servicoService.cadastrar(servico)
        );

        assertEquals("Já existe um serviço com este nome", exception.getMessage());
        verify(servicoRepository).existsByNome("Corte de Cabelo");
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve listar todos os serviços ordenados por nome")
    void deveListarTodosServicosOrdenadosPorNome() {
        Servico servico2 = new Servico();
        servico2.setId(2L);
        servico2.setNome("Barba");
        servico2.setPreco(30.0);
        servico2.setDuracaoMinutos(20);
        servico2.setAtivo(true);

        List<Servico> servicos = List.of(servico2, servico);
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(servicos);

        List<Servico> resultado = servicoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Barba", resultado.get(0).getNome());
        assertEquals("Corte de Cabelo", resultado.get(1).getNome());
        verify(servicoRepository).findAllByOrderByNomeAsc();
    }

    @Test
    @DisplayName("Deve atualizar serviço existente")
    void deveAtualizarServicoExistente() {
        servico.setPreco(60.0);
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        servicoService.atualizar(servico);

        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("Deve alternar status ativo do serviço")
    void deveAlternarStatusAtivoDoServico() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        Servico resultado = servicoService.alternarAtivo(1L);

        assertNotNull(resultado);
        assertFalse(resultado.isAtivo());
        verify(servicoRepository).findById(1L);
        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("Deve lançar exceção ao alternar serviço inexistente")
    void deveLancarExcecaoAoAlternarServicoInexistente() {
        when(servicoRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> servicoService.alternarAtivo(999L)
        );

        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(servicoRepository).findById(999L);
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve alternar de inativo para ativo")
    void deveAlternarDeInativoParaAtivo() {
        servico.setAtivo(false);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        Servico resultado = servicoService.alternarAtivo(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isAtivo());
        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("Deve excluir serviço por ID")
    void deveExcluirServicoPorId() {
        doNothing().when(servicoRepository).deleteById(1L);

        servicoService.excluir(1L);

        verify(servicoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir serviço vinculado a agendamentos")
    void deveLancarExcecaoAoExcluirServicoVinculadoAAgendamentos() {
        doThrow(DataIntegrityViolationException.class).when(servicoRepository).deleteById(1L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> servicoService.excluir(1L)
        );

        assertEquals("Não é possível excluir: serviço vinculado a agendamentos", exception.getMessage());
        verify(servicoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve listar serviços vazios quando não houver registros")
    void deveListarServicosVaziosQuandoNaoHouverRegistros() {
        when(servicoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of());

        List<Servico> resultado = servicoService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(servicoRepository).findAllByOrderByNomeAsc();
    }

    @Test
    @DisplayName("Deve buscar serviço com preço e duração corretos")
    void deveBuscarServicoComPrecoEDuracaoCorretos() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        Optional<Servico> resultado = servicoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(50.0, resultado.get().getPreco());
        assertEquals(30, resultado.get().getDuracaoMinutos());
    }
}

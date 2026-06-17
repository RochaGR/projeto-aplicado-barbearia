package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.repository.BarbeiroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do BarbeiroService")
class BarbeiroServiceTest {

    @Mock
    private BarbeiroRepository barbeiroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BarbeiroService barbeiroService;

    private Barbeiro barbeiro;

    @BeforeEach
    void setUp() {
        barbeiro = new Barbeiro();
        barbeiro.setId(1L);
        barbeiro.setNome("João Barbeiro");
        barbeiro.setEmail("joao@barbearia.com");
        barbeiro.setSenha("senha123");
        barbeiro.setAtivo(true);
    }

    @Test
    @DisplayName("Deve salvar barbeiro com senha codificada")
    void deveSalvarBarbeiroComSenhaCodificada() {
        when(passwordEncoder.encode("senha123")).thenReturn("senha_codificada");
        when(barbeiroRepository.save(any(Barbeiro.class))).thenReturn(barbeiro);

        Barbeiro resultado = barbeiroService.salvar(barbeiro);

        assertNotNull(resultado);
        verify(passwordEncoder).encode("senha123");
        verify(barbeiroRepository).save(barbeiro);
    }

    @Test
    @DisplayName("Deve buscar barbeiro por email com sucesso")
    void deveBuscarBarbeiroPorEmailComSucesso() {
        when(barbeiroRepository.findByEmail("joao@barbearia.com")).thenReturn(Optional.of(barbeiro));

        Optional<Barbeiro> resultado = barbeiroService.buscarPorEmail("joao@barbearia.com");

        assertTrue(resultado.isPresent());
        assertEquals("João Barbeiro", resultado.get().getNome());
        verify(barbeiroRepository).findByEmail("joao@barbearia.com");
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar barbeiro por email inexistente")
    void deveRetornarVazioAoBuscarBarbeiroPorEmailInexistente() {
        when(barbeiroRepository.findByEmail("naoexiste@barbearia.com")).thenReturn(Optional.empty());

        Optional<Barbeiro> resultado = barbeiroService.buscarPorEmail("naoexiste@barbearia.com");

        assertFalse(resultado.isPresent());
        verify(barbeiroRepository).findByEmail("naoexiste@barbearia.com");
    }

    @Test
    @DisplayName("Deve buscar barbeiro por ID com sucesso")
    void deveBuscarBarbeiroPorIdComSucesso() {
        when(barbeiroRepository.findById(1L)).thenReturn(Optional.of(barbeiro));

        Optional<Barbeiro> resultado = barbeiroService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("João Barbeiro", resultado.get().getNome());
        verify(barbeiroRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar barbeiro por ID inexistente")
    void deveRetornarVazioAoBuscarBarbeiroPorIdInexistente() {
        when(barbeiroRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Barbeiro> resultado = barbeiroService.buscarPorId(999L);

        assertFalse(resultado.isPresent());
        verify(barbeiroRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve listar todos os barbeiros")
    void deveListarTodosBarbeiros() {
        List<Barbeiro> barbeiros = List.of(barbeiro);
        when(barbeiroRepository.findAll()).thenReturn(barbeiros);

        List<Barbeiro> resultado = barbeiroService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Barbeiro", resultado.get(0).getNome());
        verify(barbeiroRepository).findAll();
    }

    @Test
    @DisplayName("Deve excluir barbeiro por ID")
    void deveExcluirBarbeiroPorId() {
        doNothing().when(barbeiroRepository).deleteById(1L);

        barbeiroService.excluir(1L);

        verify(barbeiroRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve alternar status ativo do barbeiro")
    void deveAlternarStatusAtivoDoBarbeiro() {
        when(barbeiroRepository.findById(1L)).thenReturn(Optional.of(barbeiro));
        when(barbeiroRepository.save(any(Barbeiro.class))).thenReturn(barbeiro);

        Barbeiro resultado = barbeiroService.alternarAtivo(1L);

        assertNotNull(resultado);
        assertFalse(resultado.isAtivo());
        verify(barbeiroRepository).findById(1L);
        verify(barbeiroRepository).save(barbeiro);
    }

    @Test
    @DisplayName("Deve lançar exceção ao alternar barbeiro inexistente")
    void deveLancarExcecaoAoAlternarBarbeiroInexistente() {
        when(barbeiroRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> barbeiroService.alternarAtivo(999L)
        );

        assertEquals("Barbeiro não encontrado", exception.getMessage());
        verify(barbeiroRepository).findById(999L);
        verify(barbeiroRepository, never()).save(any(Barbeiro.class));
    }

    @Test
    @DisplayName("Deve alternar de inativo para ativo")
    void deveAlternarDeInativoParaAtivo() {
        barbeiro.setAtivo(false);
        when(barbeiroRepository.findById(1L)).thenReturn(Optional.of(barbeiro));
        when(barbeiroRepository.save(any(Barbeiro.class))).thenReturn(barbeiro);

        Barbeiro resultado = barbeiroService.alternarAtivo(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isAtivo());
        verify(barbeiroRepository).save(barbeiro);
    }

    @Test
    @DisplayName("Deve listar barbeiros vazios quando não houver registros")
    void deveListarBarbeirosVaziosQuandoNaoHouverRegistros() {
        when(barbeiroRepository.findAll()).thenReturn(List.of());

        List<Barbeiro> resultado = barbeiroService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(barbeiroRepository).findAll();
    }
}

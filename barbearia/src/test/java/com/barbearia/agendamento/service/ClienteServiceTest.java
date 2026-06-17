package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@teste.com");
        cliente.setTelefone("11999999999");
        cliente.setSenha("Senha123@");
    }

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void deveCadastrarClienteComSucesso() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senha_codificada");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.cadastrarCliente(cliente);

        assertNotNull(resultado);
        assertEquals("João Silva", resultado.getNome());
        assertEquals("joao@teste.com", resultado.getEmail());
        verify(clienteRepository).existsByEmail("joao@teste.com");
        verify(passwordEncoder).encode("Senha123@");
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar cliente com email já existente")
    void deveLancarExcecaoAoCadastrarClienteComEmailExistente() {
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.cadastrarCliente(cliente)
        );

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(clienteRepository).existsByEmail("joao@teste.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Optional<Cliente> resultado = clienteService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(clienteRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar cliente por ID inexistente")
    void deveRetornarVazioAoBuscarClientePorIdInexistente() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Cliente> resultado = clienteService.buscarPorId(999L);

        assertFalse(resultado.isPresent());
        verify(clienteRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar cliente por email com sucesso")
    void deveBuscarClientePorEmailComSucesso() {
        when(clienteRepository.findByEmail("joao@teste.com")).thenReturn(Optional.of(cliente));

        Optional<Cliente> resultado = clienteService.buscarPorEmail("joao@teste.com");

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(clienteRepository).findByEmail("joao@teste.com");
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar cliente por email inexistente")
    void deveRetornarVazioAoBuscarClientePorEmailInexistente() {
        when(clienteRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        Optional<Cliente> resultado = clienteService.buscarPorEmail("naoexiste@teste.com");

        assertFalse(resultado.isPresent());
        verify(clienteRepository).findByEmail("naoexiste@teste.com");
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarTodosClientes() {
        List<Cliente> clientes = List.of(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);

        List<Cliente> resultado = clienteService.listarTodosClientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar clientes com paginação")
    void deveListarClientesComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pagina = new PageImpl<>(List.of(cliente));
        when(clienteRepository.findAll(pageable)).thenReturn(pagina);

        Page<Cliente> resultado = clienteService.listarClientes(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("João Silva", resultado.getContent().get(0).getNome());
        verify(clienteRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void deveAtualizarClienteComSucesso() {
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("João Silva Atualizado");
        clienteAtualizado.setEmail("joao@teste.com");
        clienteAtualizado.setTelefone("11988888888");
        clienteAtualizado.setSenha("NovaSenha123@");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode(anyString())).thenReturn("nova_senha_codificada");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.atualizarCliente(1L, clienteAtualizado);

        assertNotNull(resultado);
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente inexistente")
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.atualizarCliente(999L, cliente)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteRepository).findById(999L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente com email já em uso")
    void deveLancarExcecaoAoAtualizarClienteComEmailEmUso() {
        Cliente clienteAtualizado = new Cliente();
        clienteAtualizado.setNome("João Silva");
        clienteAtualizado.setEmail("outro@email.com");
        clienteAtualizado.setTelefone("11999999999");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("outro@email.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.atualizarCliente(1L, clienteAtualizado)
        );

        assertEquals("Email já está em uso", exception.getMessage());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByEmail("outro@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve excluir cliente com sucesso")
    void deveExcluirClienteComSucesso() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1L);

        clienteService.excluirCliente(1L);

        verify(clienteRepository).existsById(1L);
        verify(clienteRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir cliente inexistente")
    void deveLancarExcecaoAoExcluirClienteInexistente() {
        when(clienteRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.excluirCliente(999L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteRepository).existsById(999L);
        verify(clienteRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve pesquisar clientes por termo")
    void devePesquisarClientesPorTermo() {
        when(clienteRepository.pesquisarClientes("joao")).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.pesquisarClientes("joao");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João Silva", resultado.get(0).getNome());
        verify(clienteRepository).pesquisarClientes("joao");
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao pesquisar com termo nulo")
    void deveRetornarListaVaziaAoPesquisarComTermoNulo() {
        List<Cliente> resultado = clienteService.pesquisarClientes(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteRepository, never()).pesquisarClientes(anyString());
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao pesquisar com termo vazio")
    void deveRetornarListaVaziaAoPesquisarComTermoVazio() {
        List<Cliente> resultado = clienteService.pesquisarClientes("   ");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteRepository, never()).pesquisarClientes(anyString());
    }

    @Test
    @DisplayName("Deve cadastrar cliente sem senha")
    void deveCadastrarClienteSemSenha() {
        cliente.setSenha(null);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.cadastrarCliente(cliente);

        assertNotNull(resultado);
        verify(clienteRepository).existsByEmail("joao@teste.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(clienteRepository).save(cliente);
    }
}

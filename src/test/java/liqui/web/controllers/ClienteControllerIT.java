package liqui.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import liqui.common.AbstractIntegrationTest;
import liqui.entities.Cliente;
import liqui.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ClienteControllerIT extends AbstractIntegrationTest {

    @Autowired private ClienteRepository clienteRepository;

    private List<Cliente> clienteList = null;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();

        clienteList = new ArrayList<>();
        clienteList.add(new Cliente(null, "First Cliente"));
        clienteList.add(new Cliente(null, "Second Cliente"));
        clienteList.add(new Cliente(null, "Third Cliente"));
        clienteList = clienteRepository.saveAll(clienteList);
    }

    @Test
    void shouldFetchAllClientes() throws Exception {
        this.mockMvc
                .perform(get(""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(clienteList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindClienteById() throws Exception {
        Cliente cliente = clienteList.get(0);
        Long clienteId = cliente.getId();

        this.mockMvc
                .perform(get("/{id}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldCreateNewCliente() throws Exception {
        Cliente cliente = new Cliente(null, "New Cliente");
        this.mockMvc
                .perform(
                        post("").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewClienteWithoutText() throws Exception {
        Cliente cliente = new Cliente(null, null);

        this.mockMvc
                .perform(
                        post("").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateCliente() throws Exception {
        Cliente cliente = clienteList.get(0);
        cliente.setText("Updated Cliente");

        this.mockMvc
                .perform(
                        put("/{id}", cliente.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldDeleteCliente() throws Exception {
        Cliente cliente = clienteList.get(0);

        this.mockMvc
                .perform(delete("/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }
}

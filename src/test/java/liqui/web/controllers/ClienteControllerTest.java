package liqui.web.controllers;

import static liqui.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import liqui.entities.Cliente;
import liqui.model.response.PagedResult;
import liqui.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ClienteController.class)
@ActiveProfiles(PROFILE_TEST)
class ClienteControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ClienteService clienteService;

    @Autowired private ObjectMapper objectMapper;

    private List<Cliente> clienteList;

    @BeforeEach
    void setUp() {
        this.clienteList = new ArrayList<>();
        this.clienteList.add(new Cliente(1L, "text 1"));
        this.clienteList.add(new Cliente(2L, "text 2"));
        this.clienteList.add(new Cliente(3L, "text 3"));
    }

    @Test
    void shouldFetchAllClientes() throws Exception {
        Page<Cliente> page = new PageImpl<>(clienteList);
        PagedResult<Cliente> clientePagedResult = new PagedResult<>(page);
        given(clienteService.findAllClientes(0, 10, "id", "asc")).willReturn(clientePagedResult);

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
        Long clienteId = 1L;
        Cliente cliente = new Cliente(clienteId, "text 1");
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.of(cliente));

        this.mockMvc
                .perform(get("/{id}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingCliente() throws Exception {
        Long clienteId = 1L;
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/{id}", clienteId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewCliente() throws Exception {
        given(clienteService.saveCliente(any(Cliente.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Cliente cliente = new Cliente(1L, "some text");
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
        Long clienteId = 1L;
        Cliente cliente = new Cliente(clienteId, "Updated text");
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.of(cliente));
        given(clienteService.saveCliente(any(Cliente.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/{id}", cliente.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingCliente() throws Exception {
        Long clienteId = 1L;
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.empty());
        Cliente cliente = new Cliente(clienteId, "Updated text");

        this.mockMvc
                .perform(
                        put("/{id}", clienteId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCliente() throws Exception {
        Long clienteId = 1L;
        Cliente cliente = new Cliente(clienteId, "Some text");
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.of(cliente));
        doNothing().when(clienteService).deleteClienteById(cliente.getId());

        this.mockMvc
                .perform(delete("/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(cliente.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingCliente() throws Exception {
        Long clienteId = 1L;
        given(clienteService.findClienteById(clienteId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/{id}", clienteId)).andExpect(status().isNotFound());
    }
}

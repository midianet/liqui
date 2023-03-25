package liqui.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import java.util.List;
import java.util.Optional;
import liqui.entities.Cliente;
import liqui.model.response.PagedResult;
import liqui.repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;

    @InjectMocks private ClienteService clienteService;

    @Test
    void findAllClientes() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Cliente> clientePage = new PageImpl<>(List.of(getCliente()));
        given(clienteRepository.findAll(pageable)).willReturn(clientePage);

        // when
        PagedResult<Cliente> pagedResult = clienteService.findAllClientes(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isEqualTo(1);
        assertThat(pagedResult.totalPages()).isEqualTo(1);
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isEqualTo(1);
    }

    @Test
    void findClienteById() {
        // given
        given(clienteRepository.findById(1L)).willReturn(Optional.of(getCliente()));
        // when
        Optional<Cliente> optionalCliente = clienteService.findClienteById(1L);
        // then
        assertThat(optionalCliente).isPresent();
        Cliente cliente = optionalCliente.get();
        assertThat(cliente.getId()).isEqualTo(1L);
        assertThat(cliente.getText()).isEqualTo("junitTest");
    }

    @Test
    void saveCliente() {
        // given
        given(clienteRepository.save(getCliente())).willReturn(getCliente());
        // when
        Cliente persistedCliente = clienteService.saveCliente(getCliente());
        // then
        assertThat(persistedCliente).isNotNull();
        assertThat(persistedCliente.getId()).isEqualTo(1L);
        assertThat(persistedCliente.getText()).isEqualTo("junitTest");
    }

    @Test
    void deleteClienteById() {
        // given
        willDoNothing().given(clienteRepository).deleteById(1L);
        // when
        clienteService.deleteClienteById(1L);
        // then
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    private Cliente getCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setText("junitTest");
        return cliente;
    }
}

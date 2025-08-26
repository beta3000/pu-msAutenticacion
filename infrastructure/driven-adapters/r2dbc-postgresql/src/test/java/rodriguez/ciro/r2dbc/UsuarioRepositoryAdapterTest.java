package rodriguez.ciro.r2dbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import rodriguez.ciro.model.usuario.Usuario;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRepositoryAdapterTest {

    @Mock
    private UsuarioReactiveRepository usuarioReactiveRepository;

    @Mock
    private ObjectMapper objectMapper;

    private UsuarioRepositoryAdapter usuarioRepositoryAdapter;

    @BeforeEach
    void setUp() {
        usuarioRepositoryAdapter = new UsuarioRepositoryAdapter(usuarioReactiveRepository, objectMapper);
    }

    @Test
    void deberiaGuardarUsuarioCorrectamente() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        UsuarioEntity usuarioEntity = UsuarioEntity.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        UsuarioEntity usuarioEntityGuardado = UsuarioEntity.builder()
                .id(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .id(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        when(objectMapper.map(usuario, UsuarioEntity.class)).thenReturn(usuarioEntity);
        when(usuarioReactiveRepository.save(usuarioEntity)).thenReturn(Mono.just(usuarioEntityGuardado));
        when(objectMapper.map(usuarioEntityGuardado, Usuario.class)).thenReturn(usuarioGuardado);

        // When & Then
        StepVerifier.create(usuarioRepositoryAdapter.guardar(usuario))
                .expectNext(usuarioGuardado)
                .verifyComplete();
    }

    @Test
    void deberiaVerificarExistenciaDeCorreoElectronico() {
        // Given
        String correoElectronico = "juan.perez@email.com";

        when(usuarioReactiveRepository.existsByCorreoElectronico(correoElectronico))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(usuarioRepositoryAdapter.existePorCorreoElectronico(correoElectronico))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void deberiaRetornarFalsoCuandoCorreoElectronicoNoExiste() {
        // Given
        String correoElectronico = "nuevo.usuario@email.com";

        when(usuarioReactiveRepository.existsByCorreoElectronico(correoElectronico))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(usuarioRepositoryAdapter.existePorCorreoElectronico(correoElectronico))
                .expectNext(false)
                .verifyComplete();
    }
}
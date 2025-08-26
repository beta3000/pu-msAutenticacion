package rodriguez.ciro.usecase.registrarusuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import rodriguez.ciro.model.rol.Rol;
import rodriguez.ciro.model.rol.gateways.RolRepository;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.model.usuario.gateways.UsuarioRepository;
import rodriguez.ciro.usecase.registrarusuario.exception.EmailAlreadyExistsException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;

    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @BeforeEach
    void setUp() {
        registrarUsuarioUseCase = new RegistrarUsuarioUseCase(usuarioRepository, rolRepository);
        lenient().when(rolRepository.existePorId(anyLong())).thenReturn(Mono.just(true));
    }

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        when(rolRepository.existePorId(2L)).thenReturn(Mono.just(true));
        when(usuarioRepository.existePorCorreoElectronico("juan.perez@email.com"))
                .thenReturn(Mono.just(false));
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectNext(usuarioGuardado)
                .verifyComplete();

        verify(usuarioRepository).existePorCorreoElectronico("juan.perez@email.com");
        verify(usuarioRepository).guardar(usuario);
    }

    @Test
    void deberiaFallarCuandoNombresEsNulo() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres(null)
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El campo nombres es requerido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoNombresEsVacio() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("   ")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El campo nombres es requerido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoApellidosEsNulo() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos(null)
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El campo apellidos es requerido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoCorreoElectronicoEsNulo() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico(null)
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El campo correo electrónico es requerido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoSalarioBaseEsNulo() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(null)
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El campo salario base es requerido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoFormatoEmailEsInvalido() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("email-invalido")
                .salarioBase(new BigDecimal("3000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El formato del correo electrónico es inválido"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoSalarioEsMenorACero() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("-1000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El salario base debe estar entre 0 y 15,000,000"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoSalarioEsMayorAlMaximo() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("16000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().equals("El salario base debe estar entre 0 y 15,000,000"))
                .verify();

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deberiaFallarCuandoCorreoElectronicoYaExiste() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        when(usuarioRepository.existePorCorreoElectronico("juan.perez@email.com"))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectErrorMatches(error -> error instanceof EmailAlreadyExistsException &&
                        error.getMessage().equals("Ya existe un usuario registrado con este correo electrónico"))
                .verify();

        verify(usuarioRepository).existePorCorreoElectronico("juan.perez@email.com");
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void deberiaAceptarSalarioEnLimiteInferior() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(BigDecimal.ZERO)
                .rol(Rol.builder().idRol(2L).build())
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(BigDecimal.ZERO)
                .rol(Rol.builder().idRol(2L).build())
                .build();

        when(usuarioRepository.existePorCorreoElectronico("juan.perez@email.com"))
                .thenReturn(Mono.just(false));
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectNext(usuarioGuardado)
                .verifyComplete();
    }

    @Test
    void deberiaAceptarSalarioEnLimiteSuperior() {
        // Given
        Usuario usuario = Usuario.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("15000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("15000000"))
                .rol(Rol.builder().idRol(2L).build())
                .build();

        when(usuarioRepository.existePorCorreoElectronico("juan.perez@email.com"))
                .thenReturn(Mono.just(false));
        when(usuarioRepository.guardar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        StepVerifier.create(registrarUsuarioUseCase.registrar(usuario))
                .expectNext(usuarioGuardado)
                .verifyComplete();
    }
}
package rodriguez.ciro.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import rodriguez.ciro.api.dto.RegistrarUsuarioRequest;
import rodriguez.ciro.api.exception.GlobalExceptionHandler;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.usecase.registrarusuario.RegistrarUsuarioUseCase;
import rodriguez.ciro.usecase.registrarusuario.exception.EmailAlreadyExistsException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UsuarioController.class})
@WebFluxTest
@Import({GlobalExceptionHandler.class})
class UsuarioControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
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
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.idUsuario").isEqualTo(1)
                .jsonPath("$.nombres").isEqualTo("Juan Carlos")
                .jsonPath("$.apellidos").isEqualTo("Pérez García")
                .jsonPath("$.correoElectronico").isEqualTo("juan.perez@email.com")
                .jsonPath("$.salarioBase").isEqualTo(3000000);
    }

    @Test
    void deberiaRetornarBadRequestCuandoNombresEsNulo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres(null)
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Errores de validación en los datos de entrada")
                .jsonPath("$.details").isArray()
                .jsonPath("$.details[0]").isEqualTo("El campo nombres es requerido");
    }

    @Test
    void deberiaRetornarBadRequestCuandoEmailEsInvalido() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("email-invalido")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Errores de validación en los datos de entrada")
                .jsonPath("$.details").isArray()
                .jsonPath("$.details[0]").isEqualTo("El formato del correo electrónico es inválido");
    }

    @Test
    void deberiaRetornarBadRequestCuandoSalarioEsMayorAlMaximo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("16000000"))
                .build();

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.message").isEqualTo("Errores de validación en los datos de entrada")
                .jsonPath("$.details").isArray()
                .jsonPath("$.details[0]").isEqualTo("El salario base debe ser menor o igual a 15,000,000");
    }

    @Test
    void deberiaRetornarConflictCuandoEmailYaExiste() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.error(new EmailAlreadyExistsException("Ya existe un usuario registrado con este correo electrónico")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").isEqualTo("Ya existe un usuario registrado con este correo electrónico")
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void deberiaRetornarInternalServerErrorParaExcepcionesGenericas() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.error(new RuntimeException("Error inesperado")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("Ha ocurrido un error interno. Intente nuevamente.")
                .jsonPath("$.status").isEqualTo(500);
    }

    @Test
    void deberiaAceptarSalarioEnLimiteSuperior() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("15000000"))
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("15000000"))
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.salarioBase").isEqualTo(15000000);
    }

    @Test
    void deberiaAceptarSalarioEnLimiteInferior() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(BigDecimal.ZERO)
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(BigDecimal.ZERO)
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.just(usuarioGuardado));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.salarioBase").isEqualTo(0);
    }
}

package rodriguez.ciro.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import rodriguez.ciro.api.dto.RegistrarUsuarioRequest;
import rodriguez.ciro.api.dto.RolDto;
import rodriguez.ciro.api.exception.GlobalExceptionHandler;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.usecase.registrarusuario.RegistrarUsuarioUseCase;
import rodriguez.ciro.usecase.registrarusuario.exception.EmailAlreadyExistsException;
import rodriguez.ciro.usecase.registrarusuario.exception.DocumentoAlreadyExistsException;

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

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @Test
    void deberiaRegistrarUsuarioCorrectamente() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .direccion("Calle 123 #45-67")
                .telefono("3001234567")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
                .build();

        Usuario usuarioGuardado = Usuario.builder()
                .idUsuario(1L)
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
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
                .jsonPath("$.tipoDocumento").isEqualTo("CC")
                .jsonPath("$.numeroDocumento").isEqualTo("12345678")
                .jsonPath("$.correoElectronico").isEqualTo("juan.perez@email.com")
                .jsonPath("$.salarioBase").isEqualTo(3000000);
    }

    @Test
    void deberiaRetornarBadRequestCuandoNombresEsNulo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres(null)
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
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
    void deberiaRetornarBadRequestCuandoTipoDocumentoEsNulo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento(null)
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
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
                .jsonPath("$.details[0]").isEqualTo("El campo tipo de documento es requerido");
    }

    @Test
    void deberiaRetornarBadRequestCuandoNumeroDocumentoEsNulo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento(null)
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
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
                .jsonPath("$.details[0]").isEqualTo("El campo número de documento es requerido");
    }

    @Test
    void deberiaRetornarConflictCuandoCorreoElectronicoYaExiste() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.error(new EmailAlreadyExistsException(
                        "Ya existe un usuario registrado con este correo electrónico")));

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
    void deberiaRetornarConflictCuandoDocumentoYaExiste() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
                .build();

        when(registrarUsuarioUseCase.registrar(any(Usuario.class)))
                .thenReturn(Mono.error(new DocumentoAlreadyExistsException(
                        "Ya existe un usuario registrado con este tipo y número de documento")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").isEqualTo("Ya existe un usuario registrado con este tipo y número de documento")
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void deberiaRetornarBadRequestCuandoEmailEsInvalido() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("email-invalido")
                .salarioBase(new BigDecimal("3000000"))
                .rol(RolDto.builder().idRol(2L).build())
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
    void deberiaRetornarBadRequestCuandoSalarioEsNegativo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("-1000"))
                .rol(RolDto.builder().idRol(2L).build())
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
                .jsonPath("$.details[0]").isEqualTo("El salario base debe ser mayor o igual a 0");
    }

    @Test
    void deberiaRetornarBadRequestCuandoSalarioExcedeMaximo() {
        // Given
        RegistrarUsuarioRequest request = RegistrarUsuarioRequest.builder()
                .nombres("Juan Carlos")
                .apellidos("Pérez García")
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .correoElectronico("juan.perez@email.com")
                .salarioBase(new BigDecimal("16000000"))
                .rol(RolDto.builder().idRol(2L).build())
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
}

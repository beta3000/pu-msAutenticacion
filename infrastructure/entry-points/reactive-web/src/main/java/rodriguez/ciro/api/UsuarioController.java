package rodriguez.ciro.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rodriguez.ciro.api.dto.RegistrarUsuarioRequest;
import rodriguez.ciro.api.dto.UsuarioResponse;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.usecase.registrarusuario.RegistrarUsuarioUseCase;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API para gestión de usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "Correo electrónico ya registrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Mono<UsuarioResponse> registrarUsuario(@Valid @RequestBody RegistrarUsuarioRequest request) {
        log.info("Iniciando registro de usuario con correo: {}", request.getCorreoElectronico());

        return Mono.just(request)
                .map(this::mapToUsuario)
                .flatMap(registrarUsuarioUseCase::registrar)
                .map(this::mapToResponse)
                .doOnSuccess(response ->
                        log.info("Usuario registrado exitosamente con ID: {}", response.getIdUsuario()))
                .doOnError(error ->
                        log.error("Error al registrar usuario: {}", error.getMessage()));
    }

    private Usuario mapToUsuario(RegistrarUsuarioRequest request) {
        return Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .correoElectronico(request.getCorreoElectronico())
                .salarioBase(request.getSalarioBase())
                .build();
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .direccion(usuario.getDireccion())
                .telefono(usuario.getTelefono())
                .correoElectronico(usuario.getCorreoElectronico())
                .salarioBase(usuario.getSalarioBase())
                .build();
    }
}
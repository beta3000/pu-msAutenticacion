package rodriguez.ciro.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import rodriguez.ciro.api.dto.RolDto;
import rodriguez.ciro.api.dto.UsuarioResponse;
import rodriguez.ciro.model.rol.Rol;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.usecase.registrarusuario.RegistrarUsuarioUseCase;
import rodriguez.ciro.usecase.buscarusuario.BuscarUsuarioPorDocumentoUseCase;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API para gestión de usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final BuscarUsuarioPorDocumentoUseCase buscarUsuarioPorDocumentoUseCase;

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

    @GetMapping("/documento/{tipoDocumento}/{numeroDocumento}")
    @Operation(summary = "Buscar usuario por documento", description = "Busca un usuario por tipo y número de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Mono<UsuarioResponse> buscarUsuarioPorDocumento(
            @Parameter(description = "Tipo de documento", example = "CC") 
            @PathVariable("tipoDocumento") String tipoDocumento,
            @Parameter(description = "Número de documento", example = "12345678")
            @PathVariable("numeroDocumento") String numeroDocumento) {
        log.info("Buscando usuario con documento: {} - {}", tipoDocumento, numeroDocumento);

        return buscarUsuarioPorDocumentoUseCase.buscarPorTipoYNumeroDocumento(tipoDocumento, numeroDocumento)
                .map(this::mapToResponse)
                .doOnSuccess(response -> 
                        log.info("Usuario encontrado con ID: {}", response.getIdUsuario()))
                .doOnError(error ->
                        log.error("Error al buscar usuario: {}", error.getMessage()));
    }

    @GetMapping("/email/{correoElectronico}")
    @Operation(summary = "Buscar usuario por correo electrónico", description = "Busca un usuario por su correo electrónico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public Mono<UsuarioResponse> buscarUsuarioPorEmail(
            @Parameter(description = "Correo electrónico del usuario", example = "usuario@ejemplo.com")
            @PathVariable("correoElectronico") String correoElectronico) {
        log.info("Buscando usuario con correo: {}", correoElectronico);

        return buscarUsuarioPorDocumentoUseCase.buscarPorCorreoElectronico(correoElectronico)
                .map(this::mapToResponse)
                .doOnSuccess(response -> 
                        log.info("Usuario encontrado por email con ID: {}", response.getIdUsuario()))
                .doOnError(error ->
                        log.error("Error al buscar usuario por email: {}", error.getMessage()));
    }

    private Usuario mapToUsuario(RegistrarUsuarioRequest request) {
        return Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .correoElectronico(request.getCorreoElectronico())
                .salarioBase(request.getSalarioBase())
                .rol(Rol.builder().idRol(request.getRol().getIdRol()).build())
                .build();
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        RolDto rolDto = null;
        if (usuario.getRol() != null) {
            rolDto = RolDto.builder()
                    .idRol(usuario.getRol().getIdRol())
                    .nombre(usuario.getRol().getNombre())
                    .descripcion(usuario.getRol().getDescripcion())
                    .build();
        }
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .tipoDocumento(usuario.getTipoDocumento())
                .numeroDocumento(usuario.getNumeroDocumento())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .direccion(usuario.getDireccion())
                .telefono(usuario.getTelefono())
                .correoElectronico(usuario.getCorreoElectronico())
                .salarioBase(usuario.getSalarioBase())
                .rol(rolDto)
                .build();
    }
}
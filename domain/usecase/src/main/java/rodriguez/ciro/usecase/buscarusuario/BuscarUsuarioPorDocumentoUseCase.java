package rodriguez.ciro.usecase.buscarusuario;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.model.usuario.gateways.UsuarioRepository;
import rodriguez.ciro.usecase.buscarusuario.exception.UsuarioNoEncontradoException;

@AllArgsConstructor
public class BuscarUsuarioPorDocumentoUseCase {

    private final UsuarioRepository usuarioRepository;

    public Mono<Usuario> buscarPorTipoYNumeroDocumento(String tipoDocumento, String numeroDocumento) {
        return usuarioRepository.buscarPorTipoYNumeroDocumento(tipoDocumento, numeroDocumento)
                .switchIfEmpty(Mono.error(new UsuarioNoEncontradoException("Usuario no encontrado con tipo documento: " + tipoDocumento + " y número: " + numeroDocumento)));
    }

    public Mono<Usuario> buscarPorCorreoElectronico(String correoElectronico) {
        return usuarioRepository.buscarPorCorreoElectronico(correoElectronico)
                .switchIfEmpty(Mono.error(new UsuarioNoEncontradoException("Usuario no encontrado con correo electrónico: " + correoElectronico)));
    }
}
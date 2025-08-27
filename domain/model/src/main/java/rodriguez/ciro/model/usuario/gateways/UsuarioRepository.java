package rodriguez.ciro.model.usuario.gateways;

import reactor.core.publisher.Mono;
import rodriguez.ciro.model.usuario.Usuario;

public interface UsuarioRepository {
    Mono<Usuario> guardar(Usuario usuario);

    Mono<Boolean> existePorCorreoElectronico(String correoElectronico);

    Mono<Boolean> existePorTipoYNumeroDocumento(String tipoDocumento, String numeroDocumento);

    Mono<Usuario> buscarPorTipoYNumeroDocumento(String tipoDocumento, String numeroDocumento);

    Mono<Usuario> buscarPorCorreoElectronico(String correoElectronico);
}

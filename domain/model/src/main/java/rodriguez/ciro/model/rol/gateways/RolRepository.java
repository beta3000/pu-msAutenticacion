package rodriguez.ciro.model.rol.gateways;

import reactor.core.publisher.Mono;

public interface RolRepository {
    Mono<Boolean> existePorId(Long idRol);
}

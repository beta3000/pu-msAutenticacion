package rodriguez.ciro.r2dbc.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import rodriguez.ciro.model.rol.gateways.RolRepository;

@Repository
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepository {

    private final RolReactiveRepository repository;

    @Override
    public Mono<Boolean> existePorId(Long idRol) {
        if (idRol == null) {
            return Mono.just(false);
        }
        return repository.existsById(idRol);
    }
}

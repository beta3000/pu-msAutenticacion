package rodriguez.ciro.r2dbc.repository;

import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import rodriguez.ciro.model.rol.Rol;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.model.usuario.gateways.UsuarioRepository;
import rodriguez.ciro.r2dbc.entity.UsuarioEntity;
import rodriguez.ciro.r2dbc.helper.ReactiveAdapterOperations;

@Slf4j
@Repository
public class UsuarioRepositoryAdapter extends ReactiveAdapterOperations<
        Usuario,
        UsuarioEntity,
        Long,
        UsuarioReactiveRepository
        > implements UsuarioRepository {
    public UsuarioRepositoryAdapter(UsuarioReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Usuario.class));
    }

    @Override
    public Mono<Usuario> guardar(Usuario usuario) {
        log.debug("Guardando usuario en base de datos");
        return Mono.just(usuario)
                .map(u -> {
                    UsuarioEntity entity = mapper.map(u, UsuarioEntity.class);
                    if (u.getRol() != null) {
                        entity.setIdRol(u.getRol().getIdRol());
                    }
                    return entity;
                })
                .flatMap(repository::save)
                .map(usuarioData -> {
                    Usuario domain = mapper.map(usuarioData, Usuario.class);
                    if (usuarioData.getIdRol() != null) {
                        domain.setRol(Rol.builder().idRol(usuarioData.getIdRol()).build());
                    }
                    return domain;
                })
                .doOnSuccess(u -> log.debug("Usuario guardado exitosamente con ID: {}", u.getIdUsuario()));
    }

    @Override
    public Mono<Boolean> existePorCorreoElectronico(String correoElectronico) {
        log.debug("Verificando existencia de usuario con correo: {}", correoElectronico);
        return repository.existsByCorreoElectronico(correoElectronico)
                .doOnNext(existe -> log.debug("Usuario con correo {} existe: {}", correoElectronico, existe));
    }

    @Override
    public Mono<Boolean> existePorTipoYNumeroDocumento(String tipoDocumento, String numeroDocumento) {
        log.debug("Verificando existencia de usuario con documento: {} - {}", tipoDocumento, numeroDocumento);
        return repository.existsByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .doOnNext(existe -> log.debug("Usuario con documento {} - {} existe: {}", tipoDocumento, numeroDocumento, existe));
    }
}

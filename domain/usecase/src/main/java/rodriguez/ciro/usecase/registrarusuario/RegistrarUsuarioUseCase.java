package rodriguez.ciro.usecase.registrarusuario;

import reactor.core.publisher.Mono;
import rodriguez.ciro.model.usuario.Usuario;
import rodriguez.ciro.model.usuario.gateways.UsuarioRepository;

import java.math.BigDecimal;
import java.util.Objects;

public class RegistrarUsuarioUseCase {

    private static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");

    private final UsuarioRepository usuarioRepository;

    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Mono<Usuario> registrar(Usuario usuario) {
        return Mono.just(usuario)
                .doOnNext(this::validarCamposRequeridos)
                .doOnNext(this::validarFormatoEmail)
                .doOnNext(this::validarSalario)
                .flatMap(this::validarEmailUnico)
                .flatMap(usuarioRepository::guardar);
    }

    private void validarCamposRequeridos(Usuario usuario) {
        if (esNuloOVacio(usuario.getNombres())) {
            throw new IllegalArgumentException("El campo nombres es requerido");
        }
        if (esNuloOVacio(usuario.getApellidos())) {
            throw new IllegalArgumentException("El campo apellidos es requerido");
        }
        if (esNuloOVacio(usuario.getCorreoElectronico())) {
            throw new IllegalArgumentException("El campo correo electrónico es requerido");
        }
        if (Objects.isNull(usuario.getSalarioBase())) {
            throw new IllegalArgumentException("El campo salario base es requerido");
        }
    }

    private void validarFormatoEmail(Usuario usuario) {
        String email = usuario.getCorreoElectronico();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido");
        }
    }

    private void validarSalario(Usuario usuario) {
        BigDecimal salario = usuario.getSalarioBase();
        if (salario.compareTo(SALARIO_MINIMO) < 0 || salario.compareTo(SALARIO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El salario base debe estar entre 0 y 15,000,000");
        }
    }

    private Mono<Usuario> validarEmailUnico(Usuario usuario) {
        return usuarioRepository.existePorCorreoElectronico(usuario.getCorreoElectronico())
                .flatMap(existe -> {
                    if (Boolean.TRUE.equals(existe)) {
                        return Mono.error(new IllegalArgumentException(
                                "Ya existe un usuario registrado con este correo electrónico"));
                    }
                    return Mono.just(usuario);
                });
    }

    private boolean esNuloOVacio(String valor) {
        return Objects.isNull(valor) || valor.trim().isEmpty();
    }
}

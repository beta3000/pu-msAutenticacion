package rodriguez.ciro.model.usuario;

import lombok.*;
import rodriguez.ciro.model.rol.Rol;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Usuario {
    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private BigDecimal salarioBase;
    private Rol rol;
}

package rodriguez.ciro.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolDto {
    @NotNull(message = "El campo rol.idRol es requerido")
    private Long idRol;
    private String nombre;
    private String descripcion;
}

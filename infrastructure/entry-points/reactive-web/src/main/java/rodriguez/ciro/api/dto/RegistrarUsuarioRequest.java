package rodriguez.ciro.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarUsuarioRequest {
    
    @NotBlank(message = "El campo nombres es requerido")
    private String nombres;
    
    @NotBlank(message = "El campo apellidos es requerido")
    private String apellidos;
    
    @NotBlank(message = "El campo tipo de documento es requerido")
    private String tipoDocumento;

    @NotBlank(message = "El campo número de documento es requerido")
    private String numeroDocumento;

    private LocalDate fechaNacimiento;
    
    private String direccion;
    
    private String telefono;
    
    @NotBlank(message = "El campo correo electrónico es requerido")
    @Email(message = "El formato del correo electrónico es inválido")
    private String correoElectronico;
    
    @NotNull(message = "El campo salario base es requerido")
    @DecimalMin(value = "0", message = "El salario base debe ser mayor o igual a 0")
    @DecimalMax(value = "15000000", message = "El salario base debe ser menor o igual a 15,000,000")
    private BigDecimal salarioBase;

    @NotNull(message = "El campo rol es requerido")
    private RolDto rol;
}
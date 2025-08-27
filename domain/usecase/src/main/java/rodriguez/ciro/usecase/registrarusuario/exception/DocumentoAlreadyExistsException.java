package rodriguez.ciro.usecase.registrarusuario.exception;

public class DocumentoAlreadyExistsException extends RuntimeException {
    public DocumentoAlreadyExistsException(String message) {
        super(message);
    }
}

package org.example.restaurant.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    private final String field;
    private final String errorCode;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
        this.errorCode = "VALIDATION_ERROR";
    }

    public ValidationException(String message, String field, String errorCode) {
        super(message);
        this.field = field;
        this.errorCode = errorCode;
    }

    public String getField() {
        return field;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ValidationException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (field != null) {
            sb.append(", field='").append(field).append('\'');
        }
        sb.append(", errorCode='").append(errorCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

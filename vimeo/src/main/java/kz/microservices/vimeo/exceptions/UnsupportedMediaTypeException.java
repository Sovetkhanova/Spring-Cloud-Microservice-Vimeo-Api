package kz.microservices.vimeo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNSUPPORTED_MEDIA_TYPE, reason = "Media format is not supported.")
public class UnsupportedMediaTypeException extends RuntimeException {
    public UnsupportedMediaTypeException(String msg) {
        super(msg);
    }
}
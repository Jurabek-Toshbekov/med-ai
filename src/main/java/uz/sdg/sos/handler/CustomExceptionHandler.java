package uz.sdg.sos.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.sdg.sos.base.ApiResponse;

import java.nio.file.AccessDeniedException;


@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class CustomExceptionHandler {

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ApiResponse<?> handleGeneralApiException(AccessDeniedException exception) {
        log.error("Handled Exception {0}", exception);
        return new ApiResponse<>(false, exception.getMessage());
    }

}
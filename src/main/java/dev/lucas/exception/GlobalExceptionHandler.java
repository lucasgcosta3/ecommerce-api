package dev.lucas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PoolCheioException.class)
    public ResponseEntity<Map<String, String>> handlePoolCheio() {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "POOL_CHEIO"));
    }

    @ExceptionHandler(CotaPessoalException.class)
    public ResponseEntity<Map<String, String>> handleCotaPessoal() {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "COTA_PESSOAL"));
    }
}

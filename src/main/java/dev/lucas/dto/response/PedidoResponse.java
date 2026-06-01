package dev.lucas.dto.response;

import dev.lucas.enums.Status;

public record PedidoResponse (
        Long id,
        Status status
){
}

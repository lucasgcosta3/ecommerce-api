package dev.lucas.dto.request;

import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull(message = "the field 'produtoId' is required")
        Long produtoId
) {
}

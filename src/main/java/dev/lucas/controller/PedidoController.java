package dev.lucas.controller;

import dev.lucas.dto.request.PedidoRequest;
import dev.lucas.dto.response.PedidoResponse;
import dev.lucas.entity.PedidoEntity;
import dev.lucas.security.SecurityUserDetails;
import dev.lucas.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entidades")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@RequestBody PedidoRequest request,
                                                @AuthenticationPrincipal SecurityUserDetails userDetails) {
        PedidoEntity pedido = service.criarRascunho(request.produtoId(), userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PedidoResponse(pedido.getId(), pedido.getStatus()));
    }

    @PostMapping("/{id}/submeter")
    public ResponseEntity<PedidoResponse> submeter(@PathVariable Long id,
                                                   @AuthenticationPrincipal SecurityUserDetails userDetails) {
        PedidoEntity pedido = service.submeter(id, userDetails.getUser());
        return ResponseEntity.ok(new PedidoResponse(pedido.getId(), pedido.getStatus()));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<PedidoResponse> finalizar(@PathVariable Long id,
                                                    @AuthenticationPrincipal SecurityUserDetails userDetails) {
        PedidoEntity pedido = service.finalizar(id, userDetails.getUser());
        return ResponseEntity.ok(new PedidoResponse(pedido.getId(), pedido.getStatus()));
    }

    // Bug 3 corrigido: GET /entidades/{id} estava faltando
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscar(@PathVariable Long id,
                                                 @AuthenticationPrincipal SecurityUserDetails userDetails) {
        PedidoEntity pedido = service.buscar(id, userDetails.getUser());
        return ResponseEntity.ok(new PedidoResponse(pedido.getId(), pedido.getStatus()));
    }
}

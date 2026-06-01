package dev.lucas.service;

import dev.lucas.entity.PedidoEntity;
import dev.lucas.entity.PoolControl;
import dev.lucas.entity.Produto;
import dev.lucas.entity.User;
import dev.lucas.enums.Status;
import dev.lucas.exception.CotaPessoalException;
import dev.lucas.exception.PoolCheioException;
import dev.lucas.repository.PedidoRepository;
import dev.lucas.repository.PoolControlRepository;
import dev.lucas.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PoolControlRepository poolRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * ESTRATÉGIA DE CONCORRÊNCIA:
     *
     * 1. Adquirimos PESSIMISTIC_WRITE lock na linha do PoolControl (SELECT FOR UPDATE).
     *    Isso serializa todas as threads que tentam submeter simultaneamente no banco.
     *    Somente uma transação por vez avança a partir daqui.
     *
     * 2. Com o lock em mãos, verificamos pool global E cota pessoal dentro da mesma
     *    transação. Isso elimina a race condition entre o count() e o save(): nenhuma
     *    outra thread pode inserir/alterar registros PROCESSANDO para este usuário
     *    enquanto temos o lock, pois elas também tentarão o SELECT FOR UPDATE e ficarão
     *    bloqueadas.
     *
     * 3. Por que não usar apenas @Version (optimistic locking)?
     *    Sob alta contenção, optimistic locking causaria muitos rollbacks e retentativas,
     *    gerando erros 500. O pessimistic locking é mais justo: fila ordenada pelo banco.
     *
     * 4. A transação usa SERIALIZABLE implicitamente pelo lock, garantindo que os 100
     *    slots nunca sejam ultrapassados, mesmo com centenas de threads concorrentes.
     */

    @Transactional
    public PedidoEntity criarRascunho(Long produtoId, User user) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        PedidoEntity pedido = PedidoEntity.builder()
                .produto(produto)
                .user(user)
                .status(Status.RASCUNHO)
                .build();

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoEntity submeter(Long pedidoId, User user) {

        PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Blindagem de propriedade: retorna 404 se não for o dono
        if (!pedido.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Bug 6 corrigido: valida que pedido está em RASCUNHO antes de qualquer coisa
        if (pedido.getStatus() != Status.RASCUNHO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // Bug 5 corrigido: O lock é adquirido ANTES das verificações de pool e cota.
        // Ambas as checagens agora ocorrem dentro da mesma seção crítica serializada.
        PoolControl pool = poolRepository.findByIdWithLock(1L).orElseThrow();

        // Verificação do pool global
        if (pool.getVagasOcupadas() >= pool.getCapacidadeMaxima()) {
            throw new PoolCheioException();
        }

        // Verificação da cota pessoal — segura pois estamos dentro do lock
        Integer emProcessamento = pedidoRepository.countByUserIdAndStatus(user.getId(), Status.PROCESSANDO);
        if (emProcessamento >= 2) {
            throw new CotaPessoalException();
        }

        pedido.setStatus(Status.PROCESSANDO);
        pool.setVagasOcupadas(pool.getVagasOcupadas() + 1);

        pedidoRepository.save(pedido);
        poolRepository.save(pool);

        return pedido;
    }

    @Transactional
    public PedidoEntity finalizar(Long pedidoId, User user) {
        PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!pedido.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Bug 4 corrigido: validar que está PROCESSANDO antes de liberar vaga
        if (pedido.getStatus() != Status.PROCESSANDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        // Lock para garantir consistência ao decrementar o pool
        PoolControl pool = poolRepository.findByIdWithLock(1L).orElseThrow();

        pedido.setStatus(Status.CONCLUIDO);
        pool.setVagasOcupadas(pool.getVagasOcupadas() - 1);

        // Bug 4 corrigido: faltavam os dois saves — pool e pedido nunca eram persistidos
        pedidoRepository.save(pedido);
        poolRepository.save(pool);

        return pedido;
    }

    @Transactional(readOnly = true)
    public PedidoEntity buscar(Long pedidoId, User user) {
        PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Blindagem de propriedade: retorna 404 se não for o dono
        if (!pedido.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return pedido;
    }
}

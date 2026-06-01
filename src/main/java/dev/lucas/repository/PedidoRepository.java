package dev.lucas.repository;

import dev.lucas.entity.PedidoEntity;
import dev.lucas.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    Integer countByUserIdAndStatus(Long userId, Status status);
}
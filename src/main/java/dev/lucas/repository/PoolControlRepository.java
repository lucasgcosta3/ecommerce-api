package dev.lucas.repository;

import dev.lucas.entity.PoolControl;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PoolControlRepository extends JpaRepository<PoolControl, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PoolControl p WHERE p.id = :id")
    Optional<PoolControl> findByIdWithLock(Long id);
}

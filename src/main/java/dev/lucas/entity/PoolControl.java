package dev.lucas.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolControl {

    @Id
    private Long id;

    private Integer vagasOcupadas;

    private Integer capacidadeMaxima;
}

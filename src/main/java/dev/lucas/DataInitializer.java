package dev.lucas;

import dev.lucas.entity.PoolControl;
import dev.lucas.entity.Produto;
import dev.lucas.repository.PoolControlRepository;
import dev.lucas.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PoolControlRepository poolControlRepository;
    private final ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Garante que o registro do pool existe com id=1 ao subir a aplicação
        if (!poolControlRepository.existsById(1L)) {
            PoolControl pool = PoolControl.builder().id(1L).vagasOcupadas(0).capacidadeMaxima(100).build();
            poolControlRepository.save(pool);
            log.info("Pool inicializado: 0/100 vagas ocupadas.");
        } else {
            log.info("Pool já inicializado.");
        }

        if (produtoRepository.count() == 0) {

            produtoRepository.saveAll(List.of(
                    Produto.builder().nome("Notebook Gamer").build(),
                    Produto.builder().nome("PlayStation 5").build(),
                    Produto.builder().nome("iPhone").build(),
                    Produto.builder().nome("Monitor 27").build(),
                    Produto.builder().nome("RTX 5070").build()
            ));

            log.info("Produtos inicializados.");
        } else {
            log.info("Produtos já inicializados");
        }
    }
}


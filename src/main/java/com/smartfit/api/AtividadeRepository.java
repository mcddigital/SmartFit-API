
package com.smartfit.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AtividadeRepository
        extends JpaRepository<Atividade, Long> {

    List<Atividade> findByUsuarioIdOrderByDataDesc(
            Long usuarioId
    );
}

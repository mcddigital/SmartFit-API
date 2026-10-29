
package com.smartfit.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AtividadeRepository
        extends JpaRepository<Atividade, Long> {

    List<Atividade> findByUsuarioIdOrderByDataDesc(
            Long usuarioId
    );

    Optional<Atividade> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );
}

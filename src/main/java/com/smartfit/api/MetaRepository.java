
package com.smartfit.api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MetaRepository
        extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioIdOrderByDataCriacaoDesc(
            Long usuarioId
    );

    Optional<Meta> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );
}

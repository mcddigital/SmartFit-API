
package com.smartfit.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AtividadeRepository atividades;
    private final MetaRepository metas;
    private final UsuarioRepository usuarios;

    public DashboardController(
            AtividadeRepository atividades,
            MetaRepository metas,
            UsuarioRepository usuarios) {
        this.atividades = atividades;
        this.metas = metas;
        this.usuarios = usuarios;
    }

    @GetMapping
    public Map<String, Object> consultar(
            Authentication authentication) {

        Usuario usuario = usuarios
            .findByEmail(authentication.getName())
            .orElseThrow();

        var lista = atividades
            .findByUsuarioIdOrderByDataDesc(usuario.getId());

        int totalMinutos = lista.stream()
            .mapToInt(Atividade::getDuracaoMinutos)
            .sum();

        double totalCalorias = lista.stream()
            .mapToDouble(Atividade::getCalorias)
            .sum();

        int totalMetas = metas
            .findByUsuarioIdOrderByDataCriacaoDesc(
                usuario.getId())
            .size();

        return Map.of(
            "totalAtividades", lista.size(),
            "totalMinutos", totalMinutos,
            "totalCalorias", totalCalorias,
            "totalMetas", totalMetas
        );
    }
}


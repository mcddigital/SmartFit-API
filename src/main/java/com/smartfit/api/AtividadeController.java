
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/atividades")
public class AtividadeController {

    private final AtividadeRepository atividades;
    private final UsuarioRepository usuarios;

    public AtividadeController(
            AtividadeRepository atividades,
            UsuarioRepository usuarios) {
        this.atividades = atividades;
        this.usuarios = usuarios;
    }

    public record NovaAtividade(
            @NotBlank String tipo,
            @NotNull @Positive Integer duracaoMinutos,
            @NotNull @PositiveOrZero Double calorias,
            LocalDate data) {}

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody NovaAtividade dados,
            Authentication authentication) {

        Usuario usuario = usuarios
            .findByEmail(authentication.getName())
            .orElseThrow();

        Atividade atividade = new Atividade();
        atividade.setTipo(dados.tipo());
        atividade.setDuracaoMinutos(dados.duracaoMinutos());
        atividade.setCalorias(dados.calorias());
        atividade.setData(
            dados.data() != null
                ? dados.data()
                : LocalDate.now());
        atividade.setUsuario(usuario);

        Atividade salva = atividades.save(atividade);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "id", salva.getId(),
                "tipo", salva.getTipo(),
                "duracaoMinutos", salva.getDuracaoMinutos(),
                "calorias", salva.getCalorias(),
                "data", salva.getData().toString()
            ));
    }
}

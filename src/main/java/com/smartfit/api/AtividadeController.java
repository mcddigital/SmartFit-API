
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
            LocalDate data
    ) {}

    private Usuario buscarUsuario(
            Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Autenticacao necessaria."
            );
        }

        return usuarios
            .findByEmail(authentication.getName())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario nao encontrado."
                )
            );
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody NovaAtividade dados,
            Authentication authentication) {

        Usuario usuario = buscarUsuario(authentication);

        Atividade atividade = new Atividade();
        atividade.setTipo(dados.tipo());
        atividade.setDuracaoMinutos(dados.duracaoMinutos());
        atividade.setCalorias(dados.calorias());
        atividade.setData(
            dados.data() != null
                ? dados.data()
                : LocalDate.now()
        );
        atividade.setUsuario(usuario);

        Atividade salva = atividades.save(atividade);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of(
                "id", salva.getId(),
                "tipo", salva.getTipo(),
                "duracaoMinutos", salva.getDuracaoMinutos(),
                "calorias", salva.getCalorias(),
                "data", salva.getData().toString()
            ));
    }

    @GetMapping
    public ResponseEntity<?> listar(
            Authentication authentication) {

        Usuario usuario = buscarUsuario(authentication);

        var historico = atividades
            .findByUsuarioIdOrderByDataDesc(
                usuario.getId()
            )
            .stream()
            .map(atividade -> Map.of(
                "id", atividade.getId(),
                "tipo", atividade.getTipo(),
                "duracaoMinutos", atividade.getDuracaoMinutos(),
                "calorias", atividade.getCalorias(),
                "data", atividade.getData().toString()
            ))
            .toList();

        return ResponseEntity.ok(historico);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = buscarUsuario(authentication);

        Atividade atividade = atividades
            .findByIdAndUsuarioId(
                id,
                usuario.getId()
            )
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Atividade nao encontrada."
                )
            );

        atividades.delete(atividade);

        return ResponseEntity.noContent().build();
    }
}

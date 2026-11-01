
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

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
            LocalDate data,
            @PositiveOrZero Double distancia,
            @Size(max = 2000) String observacoes
    ) {}

    public record AtividadeResposta(
            Long id,
            String tipo,
            Integer duracaoMinutos,
            Double calorias,
            LocalDate data,
            Double distancia,
            String observacoes
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

    private AtividadeResposta converter(
            Atividade atividade) {

        return new AtividadeResposta(
            atividade.getId(),
            atividade.getTipo(),
            atividade.getDuracaoMinutos(),
            atividade.getCalorias(),
            atividade.getData(),
            atividade.getDistancia(),
            atividade.getObservacoes()
        );
    }

    @PostMapping
    public ResponseEntity<AtividadeResposta> cadastrar(
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

        atividade.setDistancia(dados.distancia());
        atividade.setObservacoes(dados.observacoes());
        atividade.setUsuario(usuario);

        Atividade salva = atividades.save(atividade);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(converter(salva));
    }

    @GetMapping
    public ResponseEntity<List<AtividadeResposta>> listar(
            Authentication authentication) {

        Usuario usuario = buscarUsuario(authentication);

        var historico = atividades
            .findByUsuarioIdOrderByDataDesc(
                usuario.getId()
            )
            .stream()
            .map(this::converter)
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

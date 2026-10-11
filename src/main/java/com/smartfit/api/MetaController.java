
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaRepository metas;
    private final UsuarioRepository usuarios;

    public MetaController(
            MetaRepository metas,
            UsuarioRepository usuarios) {
        this.metas = metas;
        this.usuarios = usuarios;
    }

    public record NovaMeta(
            @NotBlank String descricao,
            @NotNull @Positive Integer objetivoMinutos) {}

    public record MetaResposta(
            Long id,
            String descricao,
            Integer objetivoMinutos,
            LocalDate dataCriacao) {}

    @PostMapping
    public ResponseEntity<MetaResposta> cadastrar(
            @Valid @RequestBody NovaMeta dados,
            Authentication authentication) {

        Usuario usuario = usuarios
            .findByEmail(authentication.getName())
            .orElseThrow();

        Meta meta = new Meta();
        meta.setDescricao(dados.descricao());
        meta.setObjetivoMinutos(dados.objetivoMinutos());
        meta.setDataCriacao(LocalDate.now());
        meta.setUsuario(usuario);

        Meta salva = metas.save(meta);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(converter(salva));
    }

    @GetMapping
    public List<MetaResposta> listar(
            Authentication authentication) {

        Usuario usuario = usuarios
            .findByEmail(authentication.getName())
            .orElseThrow();

        return metas
            .findByUsuarioIdOrderByDataCriacaoDesc(
                usuario.getId())
            .stream()
            .map(this::converter)
            .toList();
    }

    private MetaResposta converter(Meta meta) {
        return new MetaResposta(
            meta.getId(),
            meta.getDescricao(),
            meta.getObjetivoMinutos(),
            meta.getDataCriacao()
        );
    }
}

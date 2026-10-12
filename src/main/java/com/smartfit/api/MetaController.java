
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaRepository metas;
    private final UsuarioRepository usuarios;

    private static final Set<String> TIPOS =
        Set.of("steps", "minutes", "distance");

    private static final Set<String> PERIODOS =
        Set.of("daily", "weekly", "monthly");

    public MetaController(
            MetaRepository metas,
            UsuarioRepository usuarios) {

        this.metas = metas;
        this.usuarios = usuarios;
    }

    public record NovaMeta(
            String descricao,
            @Positive Integer objetivoMinutos,
            String tipo,
            @Positive Double objetivo,
            String periodo
    ) {}

    public record MetaResposta(
            Long id,
            String descricao,
            Integer objetivoMinutos,
            LocalDate dataCriacao,
            String tipo,
            Double objetivo,
            String periodo
    ) {}

    @PostMapping
    public ResponseEntity<MetaResposta> cadastrar(
            @Valid @RequestBody NovaMeta dados,
            Authentication authentication) {

        Usuario usuario = buscarUsuario(authentication);

        boolean formatoNovo =
            dados.tipo() != null ||
            dados.objetivo() != null ||
            dados.periodo() != null;

        Meta meta = new Meta();

        if (formatoNovo) {
            validarNovaMeta(dados);

            meta.setTipo(dados.tipo());
            meta.setObjetivo(dados.objetivo());
            meta.setPeriodo(dados.periodo());

            String descricao =
                dados.descricao() != null &&
                !dados.descricao().isBlank()
                    ? dados.descricao().trim()
                    : descricaoAutomatica(dados.tipo());

            meta.setDescricao(descricao);

            // Compatibilidade com o campo antigo obrigatório
            // objetivoMinutos.
            // Para metas não baseadas em minutos, o valor 1
            // é apenas um marcador técnico legado.
            int minutosLegados =
                "minutes".equals(dados.tipo())
                    ? (int) Math.ceil(dados.objetivo())
                    : 1;

            meta.setObjetivoMinutos(minutosLegados);

        } else {
            // Compatibilidade com clientes antigos da API.
            if (
                dados.descricao() == null ||
                dados.descricao().isBlank() ||
                dados.objetivoMinutos() == null ||
                dados.objetivoMinutos() <= 0
            ) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe descricao e objetivoMinutos."
                );
            }

            meta.setDescricao(dados.descricao().trim());
            meta.setObjetivoMinutos(
                dados.objetivoMinutos()
            );

            // Preenchimento do novo formato para
            // registros criados pelo endpoint legado.
            meta.setTipo("minutes");
            meta.setObjetivo(
                dados.objetivoMinutos().doubleValue()
            );
            meta.setPeriodo("daily");
        }

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

        Usuario usuario = buscarUsuario(authentication);

        return metas
            .findByUsuarioIdOrderByDataCriacaoDesc(
                usuario.getId()
            )
            .stream()
            .map(this::converter)
            .toList();
    }

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

    private void validarNovaMeta(NovaMeta dados) {

        if (
            dados.tipo() == null ||
            !TIPOS.contains(dados.tipo())
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tipo invalido. Use steps, minutes ou distance."
            );
        }

        if (
            dados.periodo() == null ||
            !PERIODOS.contains(dados.periodo())
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Periodo invalido. Use daily, weekly ou monthly."
            );
        }

        if (
            dados.objetivo() == null ||
            !Double.isFinite(dados.objetivo()) ||
            dados.objetivo() <= 0
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O objetivo deve ser maior que zero."
            );
        }

        if (
            ("minutes".equals(dados.tipo()) ||
             "steps".equals(dados.tipo())) &&
            (
                dados.objetivo() % 1 != 0 ||
                dados.objetivo() > Integer.MAX_VALUE
            )
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Passos e minutos devem ser numeros inteiros validos."
            );
        }
    }

    private String descricaoAutomatica(String tipo) {
        return switch (tipo) {
            case "steps" -> "Meta de passos";
            case "minutes" -> "Meta de minutos";
            case "distance" -> "Meta de distancia";
            default -> "Meta de atividade";
        };
    }

    private MetaResposta converter(Meta meta) {

        String tipo = meta.getTipo();

        if (tipo == null) {
            tipo = "minutes";
        }

        Double objetivo = meta.getObjetivo();

        if (objetivo == null) {
            objetivo = meta.getObjetivoMinutos()
                .doubleValue();
        }

        String periodo = meta.getPeriodo();

        if (periodo == null) {
            periodo = "daily";
        }

        return new MetaResposta(
            meta.getId(),
            meta.getDescricao(),
            meta.getObjetivoMinutos(),
            meta.getDataCriacao(),
            tipo,
            objetivo,
            periodo
        );
    }
}

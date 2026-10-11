
package com.smartfit.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioController(
            UsuarioRepository repository,
            PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public record Cadastro(
            @NotBlank String nome,
            @NotBlank @Email String email,
            @NotBlank String senha) {}

    public record UsuarioResposta(
            Long id, String nome, String email) {}

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody Cadastro dados) {

        if (repository.findByEmail(dados.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Email ja cadastrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setEmail(dados.email());
        usuario.setSenha(encoder.encode(dados.senha()));

        Usuario salvo = repository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioResposta(
                        salvo.getId(),
                        salvo.getNome(),
                        salvo.getEmail()));
    }
}

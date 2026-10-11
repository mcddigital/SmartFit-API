
package com.smartfit.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public LoginController(
            UsuarioRepository repository,
            PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String senha) {}

    @PostMapping
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest dados) {

        return repository.findByEmail(dados.email())
            .filter(usuario ->
                encoder.matches(
                    dados.senha(),
                    usuario.getSenha()
                )
            )
            .<ResponseEntity<?>>map(usuario ->
                ResponseEntity.ok(Map.of(
                    "mensagem", "Login realizado com sucesso",
                    "nome", usuario.getNome(),
                    "id", usuario.getId()
                ))
            )
            .orElseGet(() ->
                ResponseEntity.status(401)
                    .body(Map.of(
                        "erro", "Email ou senha invalidos"
                    ))
            );
    }
}

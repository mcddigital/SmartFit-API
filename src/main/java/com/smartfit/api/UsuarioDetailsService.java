
package com.smartfit.api;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService
        implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarios.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Usuario nao encontrado"));

        return User.withUsername(usuario.getEmail())
            .password(usuario.getSenha())
            .roles("USER")
            .build();
    }
}

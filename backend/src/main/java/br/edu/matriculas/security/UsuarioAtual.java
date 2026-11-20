package br.edu.matriculas.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import br.edu.matriculas.exception.NaoEncontradoException;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.UsuarioRepository;

/** Resolve o {@link Usuario} associado à sessão autenticada. */
@Component
public class UsuarioAtual {

    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario de(Authentication auth) {
        return usuarios.findByLogin(auth.getName()).orElseThrow(() -> new NaoEncontradoException("Usuário"));
    }
}

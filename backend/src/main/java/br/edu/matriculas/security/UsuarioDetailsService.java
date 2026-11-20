package br.edu.matriculas.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.matriculas.repository.UsuarioRepository;

/** Carrega o usuário do MongoDB para validação do login e da senha. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return usuarios.findByLogin(login.trim().toLowerCase())
                .map(u -> User.withUsername(u.getLogin())
                        .password(u.getSenhaHash())
                        .roles(u.getPapel().name())
                        .disabled(!u.isAtivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}

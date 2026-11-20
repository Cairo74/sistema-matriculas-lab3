package br.edu.matriculas.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.matriculas.dto.Dtos.LoginRequest;
import br.edu.matriculas.dto.Dtos.UsuarioResponse;
import br.edu.matriculas.security.UsuarioAtual;
import br.edu.matriculas.service.Mapeador;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authManager;
    private final SecurityContextRepository repository;
    private final UsuarioAtual atual;
    private final Mapeador mapeador;

    public AuthController(AuthenticationManager authManager, SecurityContextRepository repository,
            UsuarioAtual atual, Mapeador mapeador) {
        this.authManager = authManager;
        this.repository = repository;
        this.atual = atual;
        this.mapeador = mapeador;
    }

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/auth/login")
    UsuarioResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request,
            HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.login().trim().toLowerCase(), req.senha()));
        if (request.getSession(false) != null) {
            request.changeSessionId(); // evita fixação de sessão
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
        return mapeador.usuario(atual.de(auth));
    }

    @PostMapping("/auth/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/auth/me")
    UsuarioResponse me(Authentication auth) {
        return mapeador.usuario(atual.de(auth));
    }
}

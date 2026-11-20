package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByLogin(String login);
    boolean existsByLogin(String login);
    List<Usuario> findByPapel(Papel papel);
    long countByCursoId(String cursoId);
}


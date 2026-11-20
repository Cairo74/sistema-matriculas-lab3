package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface DisciplinaRepository extends MongoRepository<Disciplina, String> {
    List<Disciplina> findByCursoId(String cursoId);
    List<Disciplina> findByProfessorId(String professorId);
    boolean existsByCodigo(String codigo);
    long countByCursoId(String cursoId);
}


package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface CobrancaRepository extends MongoRepository<Cobranca, String> {
    Optional<Cobranca> findByAlunoIdAndSemestre(String alunoId, String semestre);
    List<Cobranca> findByAlunoId(String alunoId);
}


package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface MatriculaRepository extends MongoRepository<Matricula, String> {
    List<Matricula> findByAlunoId(String alunoId);
    List<Matricula> findByAlunoIdAndSemestreAndStatus(String alunoId, String semestre, StatusMatricula status);
    List<Matricula> findByOfertaIdAndStatus(String ofertaId, StatusMatricula status);
    long countByOfertaIdAndStatus(String ofertaId, StatusMatricula status);
    boolean existsByAlunoIdAndOfertaIdAndStatus(String alunoId, String ofertaId, StatusMatricula status);
    List<Matricula> findBySemestreAndStatus(String semestre, StatusMatricula status);
}


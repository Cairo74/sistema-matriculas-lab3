package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface OfertaRepository extends MongoRepository<Oferta, String> {
    List<Oferta> findBySemestre(String semestre);
    List<Oferta> findBySemestreAndCursoId(String semestre, String cursoId);
    List<Oferta> findByDisciplinaIdIn(Collection<String> disciplinaIds);
    boolean existsBySemestreAndDisciplinaId(String semestre, String disciplinaId);
}


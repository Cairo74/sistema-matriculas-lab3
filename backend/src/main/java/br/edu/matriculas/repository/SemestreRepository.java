package br.edu.matriculas.repository;

import java.util.*;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.edu.matriculas.model.*;

public interface SemestreRepository extends MongoRepository<Semestre, String> {
}


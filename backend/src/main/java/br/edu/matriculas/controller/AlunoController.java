package br.edu.matriculas.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.matriculas.dto.Dtos.CobrancaResponse;
import br.edu.matriculas.dto.Dtos.MatriculaRequest;
import br.edu.matriculas.dto.Dtos.MatriculaResponse;
import br.edu.matriculas.dto.Dtos.OfertaResponse;
import br.edu.matriculas.dto.Dtos.SemestreResponse;
import br.edu.matriculas.model.Matricula;
import br.edu.matriculas.model.StatusSemestre;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.CobrancaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.SemestreRepository;
import br.edu.matriculas.security.UsuarioAtual;
import br.edu.matriculas.service.Mapeador;
import br.edu.matriculas.service.MatriculaService;
import jakarta.validation.Valid;

/** Funcionalidades do aluno: consultar currículo, matricular-se e cancelar. */
@RestController
@RequestMapping("/api/aluno")
public class AlunoController {

    private final UsuarioAtual atual;
    private final MatriculaService matriculaService;
    private final SemestreRepository semestres;
    private final OfertaRepository ofertas;
    private final MatriculaRepository matriculas;
    private final CobrancaRepository cobrancas;
    private final Mapeador mapeador;

    public AlunoController(UsuarioAtual atual, MatriculaService matriculaService, SemestreRepository semestres,
            OfertaRepository ofertas, MatriculaRepository matriculas, CobrancaRepository cobrancas,
            Mapeador mapeador) {
        this.atual = atual;
        this.matriculaService = matriculaService;
        this.semestres = semestres;
        this.ofertas = ofertas;
        this.matriculas = matriculas;
        this.cobrancas = cobrancas;
        this.mapeador = mapeador;
    }

    /** Semestres com matrículas abertas. */
    @GetMapping("/semestres")
    List<SemestreResponse> semestresAbertos() {
        return semestres.findAll().stream().filter(s -> s.getStatus() == StatusSemestre.MATRICULAS_ABERTAS)
                .map(mapeador::semestre).toList();
    }

    /** Currículo do curso do aluno nos semestres com matrículas abertas. */
    @GetMapping("/ofertas")
    List<OfertaResponse> ofertas(Authentication auth) {
        Usuario aluno = atual.de(auth);
        return semestres.findAll().stream().filter(s -> s.getStatus() == StatusSemestre.MATRICULAS_ABERTAS)
                .flatMap(s -> ofertas.findBySemestreAndCursoId(s.getRotulo(), aluno.getCursoId()).stream())
                .map(o -> mapeador.oferta(o, aluno.getId()))
                .sorted(Comparator.comparing((OfertaResponse o) -> !o.disciplina().obrigatoria())
                        .thenComparing(o -> o.disciplina().codigo()))
                .toList();
    }

    @GetMapping("/matriculas")
    List<MatriculaResponse> minhasMatriculas(Authentication auth) {
        Usuario aluno = atual.de(auth);
        return matriculas.findByAlunoId(aluno.getId()).stream()
                .sorted(Comparator.comparing(Matricula::getCriadaEm).reversed()).map(mapeador::matricula).toList();
    }

    @PostMapping("/matriculas")
    @ResponseStatus(HttpStatus.CREATED)
    MatriculaResponse matricular(@Valid @RequestBody MatriculaRequest r, Authentication auth) {
        return mapeador.matricula(matriculaService.matricular(atual.de(auth).getId(), r.ofertaId()));
    }

    @DeleteMapping("/matriculas/{id}")
    MatriculaResponse cancelar(@PathVariable String id, Authentication auth) {
        return mapeador.matricula(matriculaService.cancelar(id, atual.de(auth)));
    }

    @GetMapping("/cobrancas")
    List<CobrancaResponse> cobrancas(Authentication auth) {
        return cobrancas.findByAlunoId(atual.de(auth).getId()).stream()
                .sorted(Comparator.comparing(c -> c.getSemestre(), Comparator.reverseOrder()))
                .map(mapeador::cobranca).toList();
    }
}

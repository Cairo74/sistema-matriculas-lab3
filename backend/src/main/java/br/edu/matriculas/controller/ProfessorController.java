package br.edu.matriculas.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.matriculas.dto.Dtos.AlunoMatriculado;
import br.edu.matriculas.dto.Dtos.OfertaResponse;
import br.edu.matriculas.exception.NaoEncontradoException;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Oferta;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.UsuarioRepository;
import br.edu.matriculas.security.UsuarioAtual;
import br.edu.matriculas.service.Mapeador;

/** Funcionalidades do professor: ver suas turmas e os alunos matriculados. */
@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

    private final UsuarioAtual atual;
    private final DisciplinaRepository disciplinas;
    private final OfertaRepository ofertas;
    private final MatriculaRepository matriculas;
    private final UsuarioRepository usuarios;
    private final Mapeador mapeador;

    public ProfessorController(UsuarioAtual atual, DisciplinaRepository disciplinas, OfertaRepository ofertas,
            MatriculaRepository matriculas, UsuarioRepository usuarios, Mapeador mapeador) {
        this.atual = atual;
        this.disciplinas = disciplinas;
        this.ofertas = ofertas;
        this.matriculas = matriculas;
        this.usuarios = usuarios;
        this.mapeador = mapeador;
    }

    @GetMapping("/ofertas")
    List<OfertaResponse> minhasOfertas(Authentication auth) {
        Usuario professor = atual.de(auth);
        List<String> ids = disciplinas.findByProfessorId(professor.getId()).stream().map(Disciplina::getId).toList();
        return ofertas.findByDisciplinaIdIn(ids).stream().map(o -> mapeador.oferta(o, null))
                .sorted(Comparator.comparing(OfertaResponse::semestre).reversed()
                        .thenComparing(o -> o.disciplina().codigo()))
                .toList();
    }

    @GetMapping("/ofertas/{ofertaId}/alunos")
    List<AlunoMatriculado> alunos(@PathVariable String ofertaId, Authentication auth) {
        Usuario professor = atual.de(auth);
        Oferta oferta = ofertas.findById(ofertaId).orElseThrow(() -> new NaoEncontradoException("Disciplina ofertada"));
        Disciplina d = disciplinas.findById(oferta.getDisciplinaId())
                .orElseThrow(() -> new NaoEncontradoException("Disciplina"));
        if (!professor.getId().equals(d.getProfessorId())) {
            throw new NaoEncontradoException("Disciplina ofertada"); // não revela turmas de outros professores
        }
        return matriculas.findByOfertaIdAndStatus(ofertaId, StatusMatricula.ATIVA).stream()
                .flatMap(m -> usuarios.findById(m.getAlunoId())
                        .map(u -> new AlunoMatriculado(u.getId(), u.getNome(), u.getLogin(), m.getCriadaEm()))
                        .stream())
                .sorted(Comparator.comparing(AlunoMatriculado::nome)).toList();
    }
}

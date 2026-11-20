package br.edu.matriculas.service;

import org.springframework.stereotype.Component;

import br.edu.matriculas.config.RegrasProperties;
import br.edu.matriculas.dto.Dtos.CobrancaResponse;
import br.edu.matriculas.dto.Dtos.CursoResponse;
import br.edu.matriculas.dto.Dtos.DisciplinaResponse;
import br.edu.matriculas.dto.Dtos.MatriculaResponse;
import br.edu.matriculas.dto.Dtos.OfertaResponse;
import br.edu.matriculas.dto.Dtos.SemestreResponse;
import br.edu.matriculas.dto.Dtos.UsuarioResponse;
import br.edu.matriculas.model.Cobranca;
import br.edu.matriculas.model.Curso;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Matricula;
import br.edu.matriculas.model.Oferta;
import br.edu.matriculas.model.Semestre;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.CursoRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.UsuarioRepository;

/** Converte entidades em DTOs de resposta, resolvendo nomes relacionados. */
@Component
public class Mapeador {

    private final CursoRepository cursos;
    private final UsuarioRepository usuarios;
    private final DisciplinaRepository disciplinas;
    private final OfertaRepository ofertas;
    private final MatriculaRepository matriculas;
    private final RegrasProperties regras;

    public Mapeador(CursoRepository cursos, UsuarioRepository usuarios, DisciplinaRepository disciplinas,
            OfertaRepository ofertas, MatriculaRepository matriculas, RegrasProperties regras) {
        this.cursos = cursos;
        this.usuarios = usuarios;
        this.disciplinas = disciplinas;
        this.ofertas = ofertas;
        this.matriculas = matriculas;
        this.regras = regras;
    }

    public UsuarioResponse usuario(Usuario u) {
        String cursoNome = u.getCursoId() == null ? null
                : cursos.findById(u.getCursoId()).map(Curso::getNome).orElse(null);
        return new UsuarioResponse(u.getId(), u.getNome(), u.getLogin(), u.getPapel(), u.getCursoId(), cursoNome);
    }

    public CursoResponse curso(Curso c) {
        return new CursoResponse(c.getId(), c.getNome(), c.getCreditos(), disciplinas.countByCursoId(c.getId()),
                usuarios.countByCursoId(c.getId()));
    }

    public DisciplinaResponse disciplina(Disciplina d) {
        String cursoNome = cursos.findById(d.getCursoId()).map(Curso::getNome).orElse(null);
        String professorNome = d.getProfessorId() == null ? null
                : usuarios.findById(d.getProfessorId()).map(Usuario::getNome).orElse(null);
        return new DisciplinaResponse(d.getId(), d.getCodigo(), d.getNome(), d.getCreditos(), d.getCursoId(),
                cursoNome, d.getProfessorId(), professorNome, d.isObrigatoria());
    }

    public SemestreResponse semestre(Semestre s) {
        return new SemestreResponse(s.getRotulo(), s.getStatus(), ofertas.findBySemestre(s.getRotulo()).size());
    }

    /** @param alunoId se informado, preenche {@code matriculaId} quando o aluno está inscrito. */
    public OfertaResponse oferta(Oferta o, String alunoId) {
        Disciplina d = disciplinas.findById(o.getDisciplinaId()).orElseThrow();
        long inscritos = matriculas.countByOfertaIdAndStatus(o.getId(), StatusMatricula.ATIVA);
        String matriculaId = null;
        if (alunoId != null) {
            matriculaId = matriculas.findByOfertaIdAndStatus(o.getId(), StatusMatricula.ATIVA).stream()
                    .filter(m -> alunoId.equals(m.getAlunoId())).map(Matricula::getId).findFirst().orElse(null);
        }
        return new OfertaResponse(o.getId(), o.getSemestre(), o.getStatus(), disciplina(d), inscritos,
                regras.vagasPorDisciplina(), regras.minimoAlunos(), inscritos >= regras.vagasPorDisciplina(),
                matriculaId);
    }

    public MatriculaResponse matricula(Matricula m) {
        Disciplina d = disciplinas.findById(m.getDisciplinaId()).orElseThrow();
        return new MatriculaResponse(m.getId(), m.getOfertaId(), m.getSemestre(), m.getStatus(), m.getTipo(),
                d.getCodigo(), d.getNome(), d.getCreditos(), m.getCriadaEm());
    }

    public CobrancaResponse cobranca(Cobranca c) {
        return new CobrancaResponse(c.getSemestre(), c.getCreditos(), c.getValor(), c.getAtualizadaEm());
    }
}

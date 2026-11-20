package br.edu.matriculas.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.edu.matriculas.config.RegrasProperties;
import br.edu.matriculas.exception.NaoEncontradoException;
import br.edu.matriculas.exception.RegraNegocioException;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Matricula;
import br.edu.matriculas.model.Oferta;
import br.edu.matriculas.model.Semestre;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.model.StatusOferta;
import br.edu.matriculas.model.StatusSemestre;
import br.edu.matriculas.repository.CursoRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.SemestreRepository;

/** Ciclo do semestre: criação, geração de currículo, abertura e fechamento de matrículas. */
@Service
public class SemestreService {

    private final SemestreRepository semestres;
    private final OfertaRepository ofertas;
    private final CursoRepository cursos;
    private final DisciplinaRepository disciplinas;
    private final MatriculaRepository matriculas;
    private final CobrancaGateway cobranca;
    private final RegrasProperties regras;

    public SemestreService(SemestreRepository semestres, OfertaRepository ofertas, CursoRepository cursos,
            DisciplinaRepository disciplinas, MatriculaRepository matriculas, CobrancaGateway cobranca,
            RegrasProperties regras) {
        this.semestres = semestres;
        this.ofertas = ofertas;
        this.cursos = cursos;
        this.disciplinas = disciplinas;
        this.matriculas = matriculas;
        this.cobranca = cobranca;
        this.regras = regras;
    }

    public Semestre criar(String rotulo) {
        if (semestres.existsById(rotulo)) {
            throw new RegraNegocioException("O semestre " + rotulo + " já existe.");
        }
        return semestres.save(new Semestre(rotulo));
    }

    /** Gera (ou complementa) o currículo de um curso no semestre. */
    public List<Oferta> gerarCurriculo(String rotulo, String cursoId, List<String> disciplinaIds) {
        Semestre semestre = buscar(rotulo);
        if (semestre.getStatus() != StatusSemestre.PLANEJADO) {
            throw new RegraNegocioException("O currículo só pode ser alterado antes da abertura das matrículas.");
        }
        cursos.findById(cursoId).orElseThrow(() -> new NaoEncontradoException("Curso"));

        Set<String> unicos = new LinkedHashSet<>(disciplinaIds);
        for (String id : unicos) {
            Disciplina d = disciplinas.findById(id).orElseThrow(() -> new NaoEncontradoException("Disciplina"));
            if (!cursoId.equals(d.getCursoId())) {
                throw new RegraNegocioException("A disciplina " + d.getCodigo() + " não pertence a este curso.");
            }
            if (!ofertas.existsBySemestreAndDisciplinaId(rotulo, id)) {
                ofertas.save(new Oferta(rotulo, cursoId, id));
            }
        }
        return ofertas.findBySemestreAndCursoId(rotulo, cursoId);
    }

    public Semestre abrirMatriculas(String rotulo) {
        Semestre s = buscar(rotulo);
        if (s.getStatus() != StatusSemestre.PLANEJADO) {
            throw new RegraNegocioException("Só é possível abrir matrículas de um semestre planejado.");
        }
        if (ofertas.findBySemestre(rotulo).isEmpty()) {
            throw new RegraNegocioException("Gere o currículo antes de abrir as matrículas.");
        }
        s.setStatus(StatusSemestre.MATRICULAS_ABERTAS);
        return semestres.save(s);
    }

    /**
     * Encerra o período de matrículas. Disciplinas com menos que o mínimo de alunos
     * são canceladas (e suas matrículas também); as demais tornam-se ativas.
     */
    public Semestre encerrarMatriculas(String rotulo) {
        Semestre s = buscar(rotulo);
        if (s.getStatus() != StatusSemestre.MATRICULAS_ABERTAS) {
            throw new RegraNegocioException("As matrículas deste semestre não estão abertas.");
        }
        Set<String> alunosAfetados = new LinkedHashSet<>();
        for (Oferta o : ofertas.findBySemestre(rotulo)) {
            List<Matricula> ativas = matriculas.findByOfertaIdAndStatus(o.getId(), StatusMatricula.ATIVA);
            if (ativas.size() < regras.minimoAlunos()) {
                o.setStatus(StatusOferta.CANCELADA);
                for (Matricula m : ativas) {
                    m.setStatus(StatusMatricula.DISCIPLINA_CANCELADA);
                    alunosAfetados.add(m.getAlunoId());
                }
                matriculas.saveAll(ativas);
            } else {
                o.setStatus(StatusOferta.ATIVA);
            }
            ofertas.save(o);
        }
        s.setStatus(StatusSemestre.ENCERRADO);
        s = semestres.save(s);
        alunosAfetados.forEach(aluno -> cobranca.notificarAlteracao(aluno, rotulo));
        return s;
    }

    private Semestre buscar(String rotulo) {
        return semestres.findById(rotulo).orElseThrow(() -> new NaoEncontradoException("Semestre"));
    }
}

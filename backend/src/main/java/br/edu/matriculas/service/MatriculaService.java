package br.edu.matriculas.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.edu.matriculas.config.RegrasProperties;
import br.edu.matriculas.exception.NaoEncontradoException;
import br.edu.matriculas.exception.RegraNegocioException;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Matricula;
import br.edu.matriculas.model.Oferta;
import br.edu.matriculas.model.Papel;
import br.edu.matriculas.model.Semestre;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.model.StatusOferta;
import br.edu.matriculas.model.StatusSemestre;
import br.edu.matriculas.model.TipoMatricula;
import br.edu.matriculas.model.Usuario;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;
import br.edu.matriculas.repository.OfertaRepository;
import br.edu.matriculas.repository.SemestreRepository;
import br.edu.matriculas.repository.UsuarioRepository;

/** Regras de matrícula e cancelamento de matrícula. */
@Service
public class MatriculaService {

    private final UsuarioRepository usuarios;
    private final OfertaRepository ofertas;
    private final SemestreRepository semestres;
    private final DisciplinaRepository disciplinas;
    private final MatriculaRepository matriculas;
    private final CobrancaGateway cobranca;
    private final RegrasProperties regras;

    public MatriculaService(UsuarioRepository usuarios, OfertaRepository ofertas, SemestreRepository semestres,
            DisciplinaRepository disciplinas, MatriculaRepository matriculas, CobrancaGateway cobranca,
            RegrasProperties regras) {
        this.usuarios = usuarios;
        this.ofertas = ofertas;
        this.semestres = semestres;
        this.disciplinas = disciplinas;
        this.matriculas = matriculas;
        this.cobranca = cobranca;
        this.regras = regras;
    }

    public Matricula matricular(String alunoId, String ofertaId) {
        Usuario aluno = usuarios.findById(alunoId)
                .filter(u -> u.getPapel() == Papel.ALUNO)
                .orElseThrow(() -> new NaoEncontradoException("Aluno"));
        Oferta oferta = ofertas.findById(ofertaId).orElseThrow(() -> new NaoEncontradoException("Disciplina ofertada"));
        exigirPeriodoAberto(oferta.getSemestre());

        if (oferta.getStatus() == StatusOferta.CANCELADA) {
            throw new RegraNegocioException("Esta disciplina foi cancelada neste semestre.");
        }
        if (!Objects.equals(oferta.getCursoId(), aluno.getCursoId())) {
            throw new RegraNegocioException("Esta disciplina não faz parte do currículo do seu curso.");
        }
        if (matriculas.existsByAlunoIdAndOfertaIdAndStatus(alunoId, ofertaId, StatusMatricula.ATIVA)) {
            throw new RegraNegocioException("Você já está matriculado nesta disciplina.");
        }

        Disciplina disciplina = disciplinas.findById(oferta.getDisciplinaId())
                .orElseThrow(() -> new NaoEncontradoException("Disciplina"));
        TipoMatricula tipo = disciplina.isObrigatoria() ? TipoMatricula.OBRIGATORIA : TipoMatricula.OPTATIVA;

        List<Matricula> ativas = matriculas.findByAlunoIdAndSemestreAndStatus(alunoId, oferta.getSemestre(),
                StatusMatricula.ATIVA);
        long doMesmoTipo = ativas.stream().filter(m -> m.getTipo() == tipo).count();
        int limite = limite(tipo);
        if (doMesmoTipo >= limite) {
            throw new RegraNegocioException(tipo == TipoMatricula.OBRIGATORIA
                    ? "Limite de " + limite + " disciplinas obrigatórias atingido."
                    : "Limite de " + limite + " disciplinas optativas atingido.");
        }
        if (matriculas.countByOfertaIdAndStatus(ofertaId, StatusMatricula.ATIVA) >= regras.vagasPorDisciplina()) {
            throw new RegraNegocioException("Turma lotada: as inscrições para esta disciplina foram encerradas.");
        }

        Matricula m = new Matricula();
        m.setAlunoId(alunoId);
        m.setOfertaId(ofertaId);
        m.setDisciplinaId(disciplina.getId());
        m.setSemestre(oferta.getSemestre());
        m.setTipo(tipo);
        m.setStatus(StatusMatricula.ATIVA);
        m.setCriadaEm(Instant.now());
        m = matriculas.save(m);

        cobranca.notificarAlteracao(alunoId, oferta.getSemestre());
        return m;
    }

    /** Cancela uma matrícula; apenas o próprio aluno ou a secretaria podem fazê-lo. */
    public Matricula cancelar(String matriculaId, Usuario solicitante) {
        Matricula m = matriculas.findById(matriculaId).orElseThrow(() -> new NaoEncontradoException("Matrícula"));
        boolean dono = Objects.equals(m.getAlunoId(), solicitante.getId());
        if (!dono && solicitante.getPapel() != Papel.SECRETARIA) {
            throw new RegraNegocioException("Você só pode cancelar as suas próprias matrículas.");
        }
        if (m.getStatus() != StatusMatricula.ATIVA) {
            throw new RegraNegocioException("Esta matrícula já não está ativa.");
        }
        exigirPeriodoAberto(m.getSemestre());

        m.setStatus(StatusMatricula.CANCELADA);
        m = matriculas.save(m);
        cobranca.notificarAlteracao(m.getAlunoId(), m.getSemestre());
        return m;
    }

    private void exigirPeriodoAberto(String rotulo) {
        Semestre s = semestres.findById(rotulo).orElseThrow(() -> new NaoEncontradoException("Semestre"));
        if (s.getStatus() != StatusSemestre.MATRICULAS_ABERTAS) {
            throw new RegraNegocioException("O período de matrículas de " + rotulo + " não está aberto.");
        }
    }

    private int limite(TipoMatricula tipo) {
        return tipo == TipoMatricula.OBRIGATORIA ? regras.maxObrigatorias() : regras.maxOptativas();
    }
}

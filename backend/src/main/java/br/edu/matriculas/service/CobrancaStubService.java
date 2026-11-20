package br.edu.matriculas.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.edu.matriculas.config.RegrasProperties;
import br.edu.matriculas.model.Cobranca;
import br.edu.matriculas.model.Disciplina;
import br.edu.matriculas.model.Matricula;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.repository.CobrancaRepository;
import br.edu.matriculas.repository.DisciplinaRepository;
import br.edu.matriculas.repository.MatriculaRepository;

/**
 * Simulação do sistema de cobranças: ao ser notificado, recalcula a cobrança
 * do semestre a partir dos créditos das matrículas ativas do aluno.
 */
@Service
public class CobrancaStubService implements CobrancaGateway {

    private static final Logger log = LoggerFactory.getLogger(CobrancaStubService.class);

    private final MatriculaRepository matriculas;
    private final DisciplinaRepository disciplinas;
    private final CobrancaRepository cobrancas;
    private final RegrasProperties regras;

    public CobrancaStubService(MatriculaRepository matriculas, DisciplinaRepository disciplinas,
            CobrancaRepository cobrancas, RegrasProperties regras) {
        this.matriculas = matriculas;
        this.disciplinas = disciplinas;
        this.cobrancas = cobrancas;
        this.regras = regras;
    }

    @Override
    public void notificarAlteracao(String alunoId, String semestre) {
        int creditos = 0;
        for (Matricula m : matriculas.findByAlunoIdAndSemestreAndStatus(alunoId, semestre, StatusMatricula.ATIVA)) {
            creditos += disciplinas.findById(m.getDisciplinaId()).map(Disciplina::getCreditos).orElse(0);
        }
        Cobranca c = cobrancas.findByAlunoIdAndSemestre(alunoId, semestre).orElseGet(Cobranca::new);
        c.setAlunoId(alunoId);
        c.setSemestre(semestre);
        c.setCreditos(creditos);
        c.setValor(regras.valorCredito().multiply(BigDecimal.valueOf(creditos)));
        c.setAtualizadaEm(Instant.now());
        cobrancas.save(c);
        log.info("[cobrancas] aluno={} semestre={} creditos={} valor={}", alunoId, semestre, creditos, c.getValor());
    }
}

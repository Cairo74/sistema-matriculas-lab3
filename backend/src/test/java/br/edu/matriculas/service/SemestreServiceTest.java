package br.edu.matriculas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.matriculas.config.RegrasProperties;
import br.edu.matriculas.exception.RegraNegocioException;
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

class SemestreServiceTest {

    private final SemestreRepository semestres = mock(SemestreRepository.class);
    private final OfertaRepository ofertas = mock(OfertaRepository.class);
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final CobrancaGateway cobranca = mock(CobrancaGateway.class);

    private SemestreService service;
    private Semestre semestre;

    @BeforeEach
    void preparar() {
        service = new SemestreService(semestres, ofertas, mock(CursoRepository.class),
                mock(DisciplinaRepository.class), matriculas, cobranca,
                new RegrasProperties(60, 3, 4, 2, BigDecimal.TEN));
        semestre = new Semestre("2026.2");
        semestre.setStatus(StatusSemestre.MATRICULAS_ABERTAS);
        when(semestres.findById("2026.2")).thenReturn(Optional.of(semestre));
        when(semestres.save(any(Semestre.class))).thenAnswer(i -> i.getArgument(0));
    }

    private List<Matricula> inscritos(String ofertaId, int n) {
        List<Matricula> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Matricula m = new Matricula();
            m.setAlunoId(ofertaId + "-aluno" + i);
            m.setOfertaId(ofertaId);
            lista.add(m);
        }
        when(matriculas.findByOfertaIdAndStatus(ofertaId, StatusMatricula.ATIVA)).thenReturn(lista);
        return lista;
    }

    @Test
    void encerramentoCancelaDisciplinasAbaixoDoMinimoEMantemAsDemais() {
        Oferta pouca = new Oferta("2026.2", "c1", "d1");
        pouca.setId("o-pouca");
        Oferta ok = new Oferta("2026.2", "c1", "d2");
        ok.setId("o-ok");
        when(ofertas.findBySemestre("2026.2")).thenReturn(List.of(pouca, ok));
        List<Matricula> poucos = inscritos("o-pouca", 2);
        inscritos("o-ok", 3);

        Semestre resultado = service.encerrarMatriculas("2026.2");

        assertEquals(StatusSemestre.ENCERRADO, resultado.getStatus());
        assertEquals(StatusOferta.CANCELADA, pouca.getStatus());
        assertEquals(StatusOferta.ATIVA, ok.getStatus());
        poucos.forEach(m -> assertEquals(StatusMatricula.DISCIPLINA_CANCELADA, m.getStatus()));
        verify(cobranca).notificarAlteracao("o-pouca-aluno0", "2026.2");
        verify(cobranca).notificarAlteracao("o-pouca-aluno1", "2026.2");
    }

    @Test
    void naoAbreMatriculasSemCurriculo() {
        semestre.setStatus(StatusSemestre.PLANEJADO);
        when(ofertas.findBySemestre("2026.2")).thenReturn(List.of());

        assertThrows(RegraNegocioException.class, () -> service.abrirMatriculas("2026.2"));
    }

    @Test
    void naoEncerraSemestreQueNaoEstaAberto() {
        semestre.setStatus(StatusSemestre.PLANEJADO);

        assertThrows(RegraNegocioException.class, () -> service.encerrarMatriculas("2026.2"));
    }
}

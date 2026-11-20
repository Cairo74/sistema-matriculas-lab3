package br.edu.matriculas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

class MatriculaServiceTest {

    private static final String SEM = "2026.2";

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final OfertaRepository ofertas = mock(OfertaRepository.class);
    private final SemestreRepository semestres = mock(SemestreRepository.class);
    private final DisciplinaRepository disciplinas = mock(DisciplinaRepository.class);
    private final MatriculaRepository matriculas = mock(MatriculaRepository.class);
    private final CobrancaGateway cobranca = mock(CobrancaGateway.class);

    private MatriculaService service;
    private Usuario aluno;
    private Semestre semestre;

    @BeforeEach
    void preparar() {
        RegrasProperties regras = new RegrasProperties(60, 3, 4, 2, new BigDecimal("100"));
        service = new MatriculaService(usuarios, ofertas, semestres, disciplinas, matriculas, cobranca, regras);

        aluno = new Usuario("Ana", "ana", "x", Papel.ALUNO, "c1");
        aluno.setId("a1");
        when(usuarios.findById("a1")).thenReturn(Optional.of(aluno));

        semestre = new Semestre(SEM);
        semestre.setStatus(StatusSemestre.MATRICULAS_ABERTAS);
        when(semestres.findById(SEM)).thenReturn(Optional.of(semestre));

        when(matriculas.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));
        when(matriculas.findByAlunoIdAndSemestreAndStatus(anyString(), anyString(), any())).thenReturn(List.of());
    }

    private Oferta oferta(String id, boolean obrigatoria) {
        Disciplina d = new Disciplina();
        d.setId("d-" + id);
        d.setCreditos(4);
        d.setObrigatoria(obrigatoria);
        Oferta o = new Oferta(SEM, "c1", d.getId());
        o.setId(id);
        when(ofertas.findById(id)).thenReturn(Optional.of(o));
        when(disciplinas.findById(d.getId())).thenReturn(Optional.of(d));
        return o;
    }

    private List<Matricula> ativas(int quantidade, TipoMatricula tipo) {
        List<Matricula> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Matricula m = new Matricula();
            m.setTipo(tipo);
            lista.add(m);
        }
        return lista;
    }

    @Test
    void matriculaValidaPersisteENotificaCobranca() {
        oferta("o1", true);

        Matricula m = service.matricular("a1", "o1");

        assertEquals(TipoMatricula.OBRIGATORIA, m.getTipo());
        assertEquals(StatusMatricula.ATIVA, m.getStatus());
        verify(cobranca).notificarAlteracao("a1", SEM);
    }

    @Test
    void impedeQuintaObrigatoria() {
        oferta("o1", true);
        when(matriculas.findByAlunoIdAndSemestreAndStatus("a1", SEM, StatusMatricula.ATIVA))
                .thenReturn(ativas(4, TipoMatricula.OBRIGATORIA));

        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o1"));
        verify(matriculas, never()).save(any());
    }

    @Test
    void impedeTerceiraOptativaMasPermiteObrigatoriaComDuasOptativas() {
        oferta("opt", false);
        oferta("obr", true);
        when(matriculas.findByAlunoIdAndSemestreAndStatus("a1", SEM, StatusMatricula.ATIVA))
                .thenReturn(ativas(2, TipoMatricula.OPTATIVA));

        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "opt"));
        assertEquals(TipoMatricula.OBRIGATORIA, service.matricular("a1", "obr").getTipo());
    }

    @Test
    void impedeMatriculaEmTurmaLotada() {
        oferta("o1", true);
        when(matriculas.countByOfertaIdAndStatus("o1", StatusMatricula.ATIVA)).thenReturn(60L);

        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o1"));
        assertEquals(true, e.getMessage().contains("lotada"));
    }

    @Test
    void impedeMatriculaForaDoPeriodo() {
        oferta("o1", true);
        semestre.setStatus(StatusSemestre.ENCERRADO);

        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o1"));
    }

    @Test
    void impedeDisciplinaCanceladaEDeOutroCurso() {
        Oferta cancelada = oferta("o1", true);
        cancelada.setStatus(StatusOferta.CANCELADA);
        Oferta outroCurso = oferta("o2", true);
        outroCurso.setCursoId("outro");

        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o1"));
        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o2"));
    }

    @Test
    void impedeMatriculaDuplicada() {
        oferta("o1", true);
        when(matriculas.existsByAlunoIdAndOfertaIdAndStatus("a1", "o1", StatusMatricula.ATIVA)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.matricular("a1", "o1"));
    }

    @Test
    void cancelamentoAtualizaStatusENotificaCobranca() {
        Matricula m = new Matricula();
        m.setId("m1");
        m.setAlunoId("a1");
        m.setSemestre(SEM);
        when(matriculas.findById("m1")).thenReturn(Optional.of(m));

        Matricula cancelada = service.cancelar("m1", aluno);

        assertEquals(StatusMatricula.CANCELADA, cancelada.getStatus());
        verify(cobranca).notificarAlteracao(eq("a1"), eq(SEM));
    }

    @Test
    void alunoNaoCancelaMatriculaDeOutro() {
        Matricula m = new Matricula();
        m.setId("m1");
        m.setAlunoId("outro-aluno");
        m.setSemestre(SEM);
        when(matriculas.findById("m1")).thenReturn(Optional.of(m));

        assertThrows(RegraNegocioException.class, () -> service.cancelar("m1", aluno));
    }
}

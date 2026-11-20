package br.edu.matriculas.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.edu.matriculas.model.Papel;
import br.edu.matriculas.model.StatusMatricula;
import br.edu.matriculas.model.StatusOferta;
import br.edu.matriculas.model.StatusSemestre;
import br.edu.matriculas.model.TipoMatricula;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/** Contratos de entrada e saída da API REST. */
public final class Dtos {

    private Dtos() {
    }

    // ---------- entrada ----------

    public record LoginRequest(@NotBlank String login, @NotBlank String senha) {
    }

    public record UsuarioRequest(@NotBlank String nome, @NotBlank String login, String senha, String cursoId) {
    }

    public record CursoRequest(@NotBlank String nome, @Min(1) int creditos) {
    }

    public record DisciplinaRequest(@NotBlank String codigo, @NotBlank String nome, @Min(1) int creditos,
            @NotBlank String cursoId, String professorId, boolean obrigatoria) {
    }

    public record SemestreRequest(@Pattern(regexp = "\\d{4}\\.[12]", message = "use o formato AAAA.S (ex.: 2026.2)") String rotulo) {
    }

    public record CurriculoRequest(@NotBlank String cursoId, @NotEmpty List<String> disciplinaIds) {
    }

    public record MatriculaRequest(@NotBlank String ofertaId) {
    }

    // ---------- saída ----------

    public record UsuarioResponse(String id, String nome, String login, Papel papel, String cursoId, String cursoNome) {
    }

    public record CursoResponse(String id, String nome, int creditos, long totalDisciplinas, long totalAlunos) {
    }

    public record DisciplinaResponse(String id, String codigo, String nome, int creditos, String cursoId,
            String cursoNome, String professorId, String professorNome, boolean obrigatoria) {
    }

    public record SemestreResponse(String rotulo, StatusSemestre status, long totalOfertas) {
    }

    public record OfertaResponse(String id, String semestre, StatusOferta status, DisciplinaResponse disciplina,
            long inscritos, int vagas, int minimo, boolean lotada, String matriculaId) {
    }

    public record MatriculaResponse(String id, String ofertaId, String semestre, StatusMatricula status,
            TipoMatricula tipo, String disciplinaCodigo, String disciplinaNome, int creditos, Instant criadaEm) {
    }

    public record CobrancaResponse(String semestre, int creditos, BigDecimal valor, Instant atualizadaEm) {
    }

    public record AlunoMatriculado(String id, String nome, String login, Instant matriculadoEm) {
    }
}

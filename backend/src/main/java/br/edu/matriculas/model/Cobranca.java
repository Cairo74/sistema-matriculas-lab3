package br.edu.matriculas.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cobrança do semestre de um aluno. Mantida pelo sistema de cobranças
 * (aqui simulado) a partir das notificações do sistema de matrículas.
 */
@Document("cobrancas")
@CompoundIndex(name = "aluno_semestre", def = "{'alunoId': 1, 'semestre': 1}", unique = true)
public class Cobranca {

    @Id
    private String id;
    private String alunoId;
    private String semestre;
    private int creditos;
    private BigDecimal valor = BigDecimal.ZERO;
    private Instant atualizadaEm = Instant.now();

    public Cobranca() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAlunoId() { return alunoId; }
    public void setAlunoId(String alunoId) { this.alunoId = alunoId; }
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Instant getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(Instant atualizadaEm) { this.atualizadaEm = atualizadaEm; }
}

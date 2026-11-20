package br.edu.matriculas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Disciplina pertencente a um curso e ministrada por um professor.
 * {@code obrigatoria} define se é matriculada como 1ª opção (obrigatória)
 * ou como alternativa (optativa).
 */
@Document("disciplinas")
public class Disciplina {

    @Id
    private String id;
    @Indexed(unique = true)
    private String codigo;
    private String nome;
    private int creditos;
    private String cursoId;
    private String professorId;
    private boolean obrigatoria;

    public Disciplina() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
    public String getCursoId() { return cursoId; }
    public void setCursoId(String cursoId) { this.cursoId = cursoId; }
    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }
    public boolean isObrigatoria() { return obrigatoria; }
    public void setObrigatoria(boolean obrigatoria) { this.obrigatoria = obrigatoria; }
}

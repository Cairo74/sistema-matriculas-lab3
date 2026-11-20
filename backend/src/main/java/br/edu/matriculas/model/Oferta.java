package br.edu.matriculas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Item do currículo de um semestre: uma disciplina oferecida a um curso.
 * O conjunto de ofertas de um (semestre, curso) é o currículo gerado pela secretaria.
 */
@Document("ofertas")
@CompoundIndex(name = "semestre_disciplina", def = "{'semestre': 1, 'disciplinaId': 1}", unique = true)
public class Oferta {

    @Id
    private String id;
    private String semestre;
    private String cursoId;
    private String disciplinaId;
    private StatusOferta status = StatusOferta.ABERTA;

    public Oferta() {
    }

    public Oferta(String semestre, String cursoId, String disciplinaId) {
        this.semestre = semestre;
        this.cursoId = cursoId;
        this.disciplinaId = disciplinaId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    public String getCursoId() { return cursoId; }
    public void setCursoId(String cursoId) { this.cursoId = cursoId; }
    public String getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(String disciplinaId) { this.disciplinaId = disciplinaId; }
    public StatusOferta getStatus() { return status; }
    public void setStatus(StatusOferta status) { this.status = status; }
}

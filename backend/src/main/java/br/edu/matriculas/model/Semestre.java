package br.edu.matriculas.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Semestre letivo (ex.: "2026.2") e seu período de matrículas. */
@Document("semestres")
public class Semestre {

    /** O próprio rótulo identifica o semestre. */
    @Id
    private String rotulo;
    private StatusSemestre status = StatusSemestre.PLANEJADO;

    public Semestre() {
    }

    public Semestre(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() { return rotulo; }
    public void setRotulo(String rotulo) { this.rotulo = rotulo; }
    public StatusSemestre getStatus() { return status; }
    public void setStatus(StatusSemestre status) { this.status = status; }
}

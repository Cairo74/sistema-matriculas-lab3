package br.edu.matriculas.model;

public enum StatusOferta {
    /** Currículo gerado, período de matrículas em andamento (ou ainda por abrir). */
    ABERTA,
    /** Fechamento do período confirmou o mínimo de alunos: a disciplina ocorrerá. */
    ATIVA,
    /** Fechamento do período não atingiu o mínimo de alunos. */
    CANCELADA
}

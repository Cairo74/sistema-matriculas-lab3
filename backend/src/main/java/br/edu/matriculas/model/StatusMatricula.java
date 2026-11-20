package br.edu.matriculas.model;

public enum StatusMatricula {
    ATIVA,
    /** Cancelada pelo aluno/secretaria durante o período de matrículas. */
    CANCELADA,
    /** Cancelada automaticamente porque a disciplina não atingiu o mínimo de alunos. */
    DISCIPLINA_CANCELADA
}

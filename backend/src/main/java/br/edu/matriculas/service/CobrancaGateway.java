package br.edu.matriculas.service;

/**
 * Porta de comunicação com o sistema de cobranças. O sistema de matrículas apenas
 * notifica; quem calcula e emite a cobrança é o sistema externo.
 */
public interface CobrancaGateway {

    /** Informa que as matrículas de um aluno em um semestre foram alteradas. */
    void notificarAlteracao(String alunoId, String semestre);
}

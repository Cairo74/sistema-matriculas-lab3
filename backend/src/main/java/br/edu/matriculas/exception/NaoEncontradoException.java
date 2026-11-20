package br.edu.matriculas.exception;

public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String recurso) {
        super(recurso + " não encontrado(a).");
    }
}

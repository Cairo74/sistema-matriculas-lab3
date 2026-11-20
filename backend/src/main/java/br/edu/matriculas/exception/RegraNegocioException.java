package br.edu.matriculas.exception;

/** Violação de uma regra de negócio (HTTP 409/422 na camada web). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

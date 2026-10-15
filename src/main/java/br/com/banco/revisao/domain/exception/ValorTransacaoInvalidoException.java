package br.com.banco.revisao.domain.exception;

/**
 * Exceção semântica lançada quando o valor de uma transação financeira é inválido
 * (por exemplo: nulo, negativo ou zerado).
 */
public class ValorTransacaoInvalidoException extends RuntimeException {

    public ValorTransacaoInvalidoException(String message) {
        super(message);
    }
}

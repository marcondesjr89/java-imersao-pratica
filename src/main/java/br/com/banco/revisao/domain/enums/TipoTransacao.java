package br.com.banco.revisao.domain.enums;

/**
 * Representa os tipos fundamentais de lançamentos e transações bancárias.
 */
public enum TipoTransacao {
    DEPOSITO("Depósito em Conta"),
    SAQUE("Saque"),
    TRANSFERENCIA_ENVIADA("Transferência Enviada"),
    TRANSFERENCIA_RECEBIDA("Transferência Recebida"),
    PIX_ENVIADO("PIX Enviado"),
    PIX_RECEBIDO("PIX Recebido");

    private final String descricao;

    TipoTransacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

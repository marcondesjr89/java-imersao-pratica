package br.com.banco.revisao.domain.model;

import br.com.banco.revisao.domain.enums.TipoTransacao;
import br.com.banco.revisao.domain.exception.ValorTransacaoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa um lançamento/transação contábil imutável no sistema bancário.
 * Implementado como Record do Java 21 com validação defensiva Fail-Fast no construtor compacto.
 */
public record Transacao(
        UUID id,
        TipoTransacao tipo,
        BigDecimal valor,
        LocalDateTime dataHora,
        String descricao
) {

    /**
     * Construtor compacto para proteção de invariantes e normalização de estado.
     */
    public Transacao {
        Objects.requireNonNull(id, "O identificador da transação não pode ser nulo");
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transação é obrigatório");
        }
        if (valor == null) {
            throw new ValorTransacaoInvalidoException("O valor da transação não pode ser nulo");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorTransacaoInvalidoException("O valor da transação deve ser estritamente positivo");
        }

        // Regra de Ouro Financeira: escala padronizada em 2 casas decimais com RoundingMode.HALF_EVEN (arredondamento bancário)
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);

        Objects.requireNonNull(dataHora, "A data e hora da transação não podem ser nulas");

        if (descricao == null || descricao.isBlank()) {
            descricao = tipo.getDescricao();
        } else {
            descricao = descricao.strip();
        }
    }

    /**
     * Método fábrica estático de conveniência para instanciar transações gerando ID e data/hora atuais.
     */
    public static Transacao criar(TipoTransacao tipo, BigDecimal valor, String descricao) {
        return new Transacao(UUID.randomUUID(), tipo, valor, LocalDateTime.now(), descricao);
    }
}

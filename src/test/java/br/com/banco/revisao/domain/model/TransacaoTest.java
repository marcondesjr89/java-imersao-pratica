package br.com.banco.revisao.domain.model;

import br.com.banco.revisao.domain.enums.TipoTransacao;
import br.com.banco.revisao.domain.exception.ValorTransacaoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transacao - Testes Unitários de Domínio")
class TransacaoTest {

    @Nested
    @DisplayName("Cenários de Sucesso (Happy Paths)")
    class HappyPaths {

        @Test
        @DisplayName("Deve criar uma transação com sucesso fornecendo todos os dados válidos")
        void deveCriarTransacaoComSucesso() {
            UUID id = UUID.randomUUID();
            LocalDateTime agora = LocalDateTime.now();
            BigDecimal valor = new BigDecimal("150.75");
            String descricao = "Depósito via PIX";

            Transacao transacao = new Transacao(id, TipoTransacao.PIX_RECEBIDO, valor, agora, descricao);

            assertThat(transacao.id()).isEqualTo(id);
            assertThat(transacao.tipo()).isEqualTo(TipoTransacao.PIX_RECEBIDO);
            assertThat(transacao.valor()).isEqualByComparingTo(new BigDecimal("150.75"));
            assertThat(transacao.valor().scale()).isEqualTo(2);
            assertThat(transacao.dataHora()).isEqualTo(agora);
            assertThat(transacao.descricao()).isEqualTo(descricao);
        }

        @Test
        @DisplayName("Deve permitir criar transação com método fábrica gerando ID e data/hora automáticos")
        void deveCriarTransacaoComMetodoFabrica() {
            BigDecimal valor = new BigDecimal("200.00");

            Transacao transacao = Transacao.criar(TipoTransacao.DEPOSITO, valor, "Depósito em dinheiro");

            assertThat(transacao.id()).isNotNull();
            assertThat(transacao.tipo()).isEqualTo(TipoTransacao.DEPOSITO);
            assertThat(transacao.valor()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(transacao.dataHora()).isNotNull();
            assertThat(transacao.descricao()).isEqualTo("Depósito em dinheiro");
        }
    }

    @Nested
    @DisplayName("Cenários de Borda e Validações Defensivas (Edge Cases)")
    class EdgeCases {

        @Test
        @DisplayName("Edge Case: Deve lançar exceção quando o valor for nulo")
        void deveLancarExcecaoQuandoValorForNulo() {
            assertThatThrownBy(() -> Transacao.criar(TipoTransacao.DEPOSITO, null, "Depósito inválido"))
                    .isInstanceOf(ValorTransacaoInvalidoException.class)
                    .hasMessageContaining("O valor da transação não pode ser nulo");
        }

        @ParameterizedTest(name = "Edge Case: Deve rejeitar valor zerado {0}")
        @ValueSource(strings = {"0", "0.0", "0.00", "0.000"})
        @DisplayName("Edge Case: Deve lançar exceção quando o valor for zero")
        void deveLancarExcecaoQuandoValorForZero(String valorZero) {
            BigDecimal valor = new BigDecimal(valorZero);

            assertThatThrownBy(() -> Transacao.criar(TipoTransacao.SAQUE, valor, "Saque zerado"))
                    .isInstanceOf(ValorTransacaoInvalidoException.class)
                    .hasMessageContaining("O valor da transação deve ser estritamente positivo");
        }

        @ParameterizedTest(name = "Edge Case: Deve rejeitar valor negativo {0}")
        @ValueSource(strings = {"-0.01", "-1.00", "-500.99"})
        @DisplayName("Edge Case: Deve lançar exceção quando o valor for negativo")
        void deveLancarExcecaoQuandoValorForNegativo(String valorNegativo) {
            BigDecimal valor = new BigDecimal(valorNegativo);

            assertThatThrownBy(() -> Transacao.criar(TipoTransacao.SAQUE, valor, "Saque negativo"))
                    .isInstanceOf(ValorTransacaoInvalidoException.class)
                    .hasMessageContaining("O valor da transação deve ser estritamente positivo");
        }

        @Test
        @DisplayName("Edge Case: Deve lançar exceção quando o tipo da transação for nulo")
        void deveLancarExcecaoQuandoTipoForNulo() {
            BigDecimal valor = new BigDecimal("100.00");

            assertThatThrownBy(() -> Transacao.criar(null, valor, "Sem tipo"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("O tipo da transação é obrigatório");
        }

        @Test
        @DisplayName("Edge Case: Deve normalizar a escala do valor para 2 casas decimais bancárias (HALF_EVEN)")
        void deveNormalizarEscalaMonetariaParaDuasCasas() {
            BigDecimal valorComUmaCasa = new BigDecimal("100.5");

            Transacao transacao = Transacao.criar(TipoTransacao.DEPOSITO, valorComUmaCasa, "Ajuste de escala");

            assertThat(transacao.valor().scale()).isEqualTo(2);
            assertThat(transacao.valor()).isEqualTo(new BigDecimal("100.50"));
        }

        @Test
        @DisplayName("Edge Case: Deve utilizar descrição padrão do tipo quando a descrição informada for nula ou em branco")
        void deveAdotarDescricaoPadraoQuandoNulaOuEmBranco() {
            BigDecimal valor = new BigDecimal("80.00");

            Transacao comDescricaoNula = Transacao.criar(TipoTransacao.SAQUE, valor, null);
            Transacao comDescricaoEmBranco = Transacao.criar(TipoTransacao.SAQUE, valor, "   ");

            assertThat(comDescricaoNula.descricao()).isEqualTo(TipoTransacao.SAQUE.getDescricao());
            assertThat(comDescricaoEmBranco.descricao()).isEqualTo(TipoTransacao.SAQUE.getDescricao());
        }

        @Test
        @DisplayName("Edge Case: Deve remover espaços em excesso (trim/strip) da descrição")
        void deveRemoverEspacosSobressalentesDaDescricao() {
            BigDecimal valor = new BigDecimal("50.00");

            Transacao transacao = Transacao.criar(TipoTransacao.DEPOSITO, valor, "   Transferência recebida de João   ");

            assertThat(transacao.descricao()).isEqualTo("Transferência recebida de João");
        }
    }
}

package payment.system.app.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.PaymentStatus;

class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
    }

    @Test
    void shouldMapWalletTransferResponseToTransactionResponse() {

        WalletTransferResponse walletResponse =
                WalletTransferResponse.builder()
                        .walletTransactionReference("WALLET_TXN_1001")
                        .senderUserId(1L)
                        .receiverUserId(2L)
                        .amount(BigDecimal.valueOf(500))
                        .build();

        TransactionResponse response =
                transactionMapper.toResponse(
                        "PAY_TXN_1001",
                        walletResponse,
                        PaymentStatus.SUCCESS);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionReference())
                .isEqualTo("PAY_TXN_1001");
        assertThat(response.getWalletTransactionReference())
                .isEqualTo("WALLET_TXN_1001");
        assertThat(response.getSenderUserId())
                .isEqualTo(1L);
        assertThat(response.getReceiverUserId())
                .isEqualTo(2L);
        assertThat(response.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.getStatus())
                .isEqualTo("SUCCESS");
        assertThat(response.getTimestamp())
                .isNotNull();
    }

    @Test
    void shouldMapTransactionEntityToTransactionResponse() {

        LocalDateTime createdAt = LocalDateTime.now();

        Transaction transaction =
                Transaction.builder()
                        .id(1L)
                        .transactionReference("PAY_TXN_2001")
                        .walletTransactionReference("WALLET_TXN_2001")
                        .senderUserId(10L)
                        .receiverUserId(20L)
                        .amount(BigDecimal.valueOf(1200))
                        .status(PaymentStatus.SUCCESS)
                        .createdAt(createdAt)
                        .build();

        TransactionResponse response =
                transactionMapper.toResponse(transaction);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionReference())
                .isEqualTo("PAY_TXN_2001");
        assertThat(response.getWalletTransactionReference())
                .isEqualTo("WALLET_TXN_2001");
        assertThat(response.getSenderUserId())
                .isEqualTo(10L);
        assertThat(response.getReceiverUserId())
                .isEqualTo(20L);
        assertThat(response.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(1200));
        assertThat(response.getStatus())
                .isEqualTo("SUCCESS");
        assertThat(response.getTimestamp())
                .isEqualTo(createdAt);
    }

    @Test
    void shouldReturnNullWhenTransactionIsNull() {

        TransactionResponse response =
                transactionMapper.toResponse((Transaction) null);

        assertThat(response).isNull();
    }

}
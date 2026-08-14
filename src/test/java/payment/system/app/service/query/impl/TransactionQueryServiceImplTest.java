package payment.system.app.service.query.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.BadRequestException;
import payment.system.app.mapper.TransactionMapper;
import payment.system.app.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionQueryServiceImpl transactionQueryService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);
    }

    @Test
    void shouldReturnPagedTransactions() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .status(PaymentStatus.SUCCESS)
                        .build();

        Transaction transaction =
                Transaction.builder()
                        .id(1L)
                        .transactionReference("TXN001")
                        .walletTransactionReference("WALLET001")
                        .senderUserId(1L)
                        .receiverUserId(2L)
                        .amount(BigDecimal.valueOf(1000))
                        .status(PaymentStatus.SUCCESS)
                        .createdAt(LocalDateTime.now())
                        .build();

        TransactionResponse response =
                TransactionResponse.builder()
                        .transactionReference("TXN001")
                        .walletTransactionReference("WALLET001")
                        .senderUserId(1L)
                        .receiverUserId(2L)
                        .amount(BigDecimal.valueOf(1000))
                        .status("SUCCESS")
                        .timestamp(transaction.getCreatedAt())
                        .build();

        Page<Transaction> page =
                new PageImpl<>(
                        List.of(transaction),
                        pageable,
                        1);

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        Page<TransactionResponse> result =
                transactionQueryService.searchTransactions(
                        request,
                        pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        assertThat(result.getContent().get(0).getTransactionReference())
                .isEqualTo("TXN001");

        assertThat(result.getContent().get(0).getStatus())
                .isEqualTo("SUCCESS");

        verify(transactionRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class));

        verify(transactionMapper)
                .toResponse(transaction);
    }

    @Test
    void shouldReturnEmptyPage() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .build();

        Page<Transaction> page =
                Page.empty(pageable);

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponse> result =
                transactionQueryService.searchTransactions(
                        request,
                        pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(transactionRepository)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class));

        verify(transactionMapper, never())
                .toResponse(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenPageableIsNull() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> transactionQueryService.searchTransactions(
                        request,
                        null));

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void shouldThrowExceptionWhenMinAmountGreaterThanMaxAmount() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .minAmount(BigDecimal.valueOf(500))
                        .maxAmount(BigDecimal.valueOf(100))
                        .build();

        assertThrows(
                BadRequestException.class,
                () -> transactionQueryService.searchTransactions(
                        request,
                        pageable));

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void shouldInvokeRepositoryOnlyOnce() {

        TransactionSearchRequest request =
                TransactionSearchRequest.builder()
                        .build();

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        transactionQueryService.searchTransactions(
                request,
                pageable);

        verify(transactionRepository, times(1))
                .findAll(
                        any(Specification.class),
                        any(Pageable.class));
    }

    @Test
    void shouldMapMultipleTransactions() {

        Transaction tx1 =
                Transaction.builder()
                        .id(1L)
                        .status(PaymentStatus.SUCCESS)
                        .build();

        Transaction tx2 =
                Transaction.builder()
                        .id(2L)
                        .status(PaymentStatus.SUCCESS)
                        .build();

        Page<Transaction> page =
                new PageImpl<>(
                        List.of(tx1, tx2),
                        pageable,
                        2);

        when(transactionRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        when(transactionMapper.toResponse(any(Transaction.class)))
                .thenReturn(
                        TransactionResponse.builder()
                                .status("SUCCESS")
                                .build());

        Page<TransactionResponse> result =
                transactionQueryService.searchTransactions(
                        TransactionSearchRequest.builder().build(),
                        pageable);

        assertThat(result.getContent()).hasSize(2);

        verify(transactionMapper, times(2))
                .toResponse(any(Transaction.class));
    }
}
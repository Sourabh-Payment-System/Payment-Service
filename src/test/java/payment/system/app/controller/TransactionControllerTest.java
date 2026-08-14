package payment.system.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.web.servlet.MockMvc;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.service.query.TransactionQueryService;
import payment.system.app.util.PageResponseUtil;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionQueryService transactionQueryService;

    @Test
    void shouldReturnTransactions() throws Exception {

        TransactionResponse response =
                TransactionResponse.builder()
                        .transactionReference("TXN001")
                        .walletTransactionReference("WALLET001")
                        .senderUserId(1L)
                        .receiverUserId(2L)
                        .amount(BigDecimal.valueOf(1000))
                        .status("SUCCESS")
                        .timestamp(LocalDateTime.now())
                        .build();

        Page<TransactionResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 20),
                        1);

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Transactions fetched successfully"))
                .andExpect(jsonPath("$.data.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.data.content[0].transactionReference")
                        .value("TXN001"))
                .andExpect(jsonPath("$.data.content[0].walletTransactionReference")
                        .value("WALLET001"))
                .andExpect(jsonPath("$.data.content[0].senderUserId")
                        .value(1))
                .andExpect(jsonPath("$.data.content[0].receiverUserId")
                        .value(2))
                .andExpect(jsonPath("$.data.content[0].status")
                        .value("SUCCESS"));

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldReturnEmptyPage() throws Exception {

        Page<TransactionResponse> page =
                Page.empty(PageRequest.of(0, 20));

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()")
                        .value(0))
                .andExpect(jsonPath("$.data.empty")
                        .value(true));

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptPagination() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptSorting() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("sort", "amount,desc"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptStatusFilter() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptSenderUserIdFilter() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("senderUserId", "1"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptReceiverUserIdFilter() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("receiverUserId", "2"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

    @Test
    void shouldAcceptAmountFilter() throws Exception {

        when(transactionQueryService.searchTransactions(
                any(),
                any()))
                .thenReturn(Page.empty());

        mockMvc.perform(
                get("/api/v1/transactions")
                        .param("minAmount", "100")
                        .param("maxAmount", "500"))
                .andExpect(status().isOk());

        verify(transactionQueryService)
                .searchTransactions(any(), any());
    }

}
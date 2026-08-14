package payment.system.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import payment.system.app.dto.ApiResponse;
import payment.system.app.dto.PageResponse;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.service.query.TransactionQueryService;
import payment.system.app.util.PageResponseUtil;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>>
    searchTransactions(

            @Valid
            @ModelAttribute
            TransactionSearchRequest request,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        var page =
                transactionQueryService.searchTransactions(
                        request,
                        pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Transactions fetched successfully",
                        PageResponseUtil.from(page)
                ));
    }

}
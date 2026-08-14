package payment.system.app.service.query.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.entity.Transaction;
import payment.system.app.mapper.TransactionMapper;
import payment.system.app.repository.TransactionRepository;
import payment.system.app.service.query.TransactionQueryService;
import payment.system.app.specification.TransactionSpecification;
import payment.system.app.validation.PageableValidator;
import payment.system.app.validation.TransactionSearchValidator;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryServiceImpl
        implements TransactionQueryService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    @Override
    public Page<TransactionResponse> searchTransactions(
            TransactionSearchRequest request,
            Pageable pageable) {

        PageableValidator.validate(pageable);

        TransactionSearchValidator.validate(request);

        log.info(
        	    "Searching transactions sender={} receiver={} status={} reference={} page={} size={} sort={}",
        	    request.getSenderUserId(),
        	    request.getReceiverUserId(),
        	    request.getStatus(),
        	    request.getTransactionReference(),
        	    pageable.getPageNumber(),
        	    pageable.getPageSize(),
        	    pageable.getSort()
        	);

        Page<Transaction> transactions =
                findTransactions(request, pageable);

        return mapToResponse(transactions);
    }

    /**
     * Fetch transactions from database.
     */
    private Page<Transaction> findTransactions(
            TransactionSearchRequest request,
            Pageable pageable) {

        return transactionRepository.findAll(
                TransactionSpecification.search(request),
                pageable);
    }

    /**
     * Convert entities to response DTOs.
     */
    private Page<TransactionResponse> mapToResponse(
            Page<Transaction> transactions) {

        return transactions.map(transactionMapper::toResponse);
    }

}
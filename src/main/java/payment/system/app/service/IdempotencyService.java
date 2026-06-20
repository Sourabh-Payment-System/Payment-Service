package payment.system.app.service;

import static payment.system.app.constants.TransactionConstants.TRANSACTION_PREFIX;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_REFERENCE_LENGTH;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.IdempotencyResult;
import payment.system.app.dto.TransferRequest;
import payment.system.app.entity.IdempotencyRecord;
import payment.system.app.enums.IdempotencyStatus;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.IdempotencyRecordNotFoundException;
import payment.system.app.repository.IdempotencyRepository;
import payment.system.app.utility.ReferenceGenerator;
import payment.system.app.utility.TransferRequestHasher;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository repository;

    private final ReferenceGenerator referenceGenerator;

    private final TransferRequestHasher requestHasher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyResult getOrCreateProcessingRecord(
            String idempotencyKey,
            TransferRequest request) {

        String transactionReference =
                TRANSACTION_PREFIX
                + referenceGenerator.generateReference(
                        TRANSACTION_REFERENCE_LENGTH);

        String requestHash =
                requestHasher.generateHash(
                        request);

        int rowsInserted =
                repository.insertIfAbsent(
                        idempotencyKey,
                        transactionReference,
                        requestHash);

        IdempotencyRecord record =
                repository.findByIdempotencyKeyForUpdate(
                                idempotencyKey)
                        .orElseThrow(() ->
                                new IdempotencyRecordNotFoundException(
                                        idempotencyKey));

        if (record.getRequestHash() != null
                && !record.getRequestHash()
                        .equals(requestHash)) {

            throw new BadRequestException(
                    "Idempotency key reused with different request");
        }

        

        return new IdempotencyResult(
                record,
                rowsInserted == 1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(
            String idempotencyKey,
            String responseJson) {

        IdempotencyRecord record =
                repository.findByIdempotencyKeyForUpdate(
                                idempotencyKey)
                        .orElseThrow(() ->
                                new IdempotencyRecordNotFoundException(
                                        idempotencyKey));

        record.setStatus(
                IdempotencyStatus.SUCCESS);

        record.setResponseJson(
                responseJson);

        record.setCompletedAt(
                LocalDateTime.now());

        repository.save(
                record);

        log.info(
                "Idempotency record marked SUCCESS. key={}",
                idempotencyKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            String idempotencyKey) {

        IdempotencyRecord record =
                repository.findByIdempotencyKeyForUpdate(
                                idempotencyKey)
                        .orElseThrow(() ->
                                new IdempotencyRecordNotFoundException(
                                        idempotencyKey));

        if (record.getStatus()
                == IdempotencyStatus.SUCCESS) {

            return;
        }

        record.setStatus(
                IdempotencyStatus.FAILED);

        record.setCompletedAt(
                LocalDateTime.now());

        repository.save(
                record);

        log.info(
                "Idempotency record marked FAILED. key={}",
                idempotencyKey);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetToProcessing(
            String idempotencyKey,
            String transactionReference) {

        IdempotencyRecord record =
                repository.findByIdempotencyKeyForUpdate(
                                idempotencyKey)
                        .orElseThrow(() ->
                                new IdempotencyRecordNotFoundException(
                                        idempotencyKey));

        record.setStatus(
                IdempotencyStatus.PROCESSING);

        record.setTransactionReference(
                transactionReference);

        record.setResponseJson(
                null);

        record.setProcessingStartedAt(
                LocalDateTime.now());

        record.setCompletedAt(
                null);

        repository.save(
                record);

        log.info(
                "Idempotency record reset to PROCESSING. key={}",
                idempotencyKey);
    }
}
package payment.system.app.service;

import static payment.system.app.constants.ErrorMessages.SENDER_RECEIVER_SAME;
import static payment.system.app.constants.LogMessages.MDC_TRANSACTION_ID;
import static payment.system.app.constants.LogMessages.MDC_TRANSACTION_REF;
import static payment.system.app.constants.LogMessages.SAME_USER_TRANSFER_ATTEMPT;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_REFERENCE_LENGTH;
import static payment.system.app.constants.TransactionConstants.TRANSACTION_PREFIX;
import static payment.system.app.constants.TransactionConstants.PROCESSING_TIMEOUT_MINUTES;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import payment.system.app.dto.IdempotencyResult;
import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.TransferRequest;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.entity.IdempotencyRecord;
import payment.system.app.entity.Transaction;
import payment.system.app.enums.ErrorCode;
import payment.system.app.enums.PaymentStatus;
import payment.system.app.exception.BadRequestException;
import payment.system.app.exception.IdempotencyRecordNotFoundException;
import payment.system.app.exception.PaymentProcessingException;
import payment.system.app.facade.WalletFacadeService;
import payment.system.app.mapper.TransactionMapper;
import payment.system.app.utility.ReferenceGenerator;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final WalletFacadeService walletFacadeService;
	private final TransactionMapper transactionMapper;
	private final TransactionService transactionService;
	private final IdempotencyService idempotencyService;
	private final ObjectMapper objectMapper;
	private final ReferenceGenerator referenceGenerator;

	public TransactionResponse transferMoney(TransferRequest request, String idempotencyKey) {

		validateIdempotencyKey(idempotencyKey);
		validateBusinessRules(request);

		Transaction transaction = null;
		boolean paymentOwnedByCurrentRequest = false;
		String transactionReference = null;

		try {

			IdempotencyResult result =
			        idempotencyService
			                .getOrCreateProcessingRecord(
			                        idempotencyKey,
			                        request);

			IdempotencyRecord record = result.getRecord();

			paymentOwnedByCurrentRequest = result.isOwnedByCurrentRequest();

			if (!paymentOwnedByCurrentRequest) {

				switch (record.getStatus()) {

				case SUCCESS:

					transactionReference = record.getTransactionReference();

					MDC.put(MDC_TRANSACTION_REF, transactionReference);

					log.info("Returning cached response. idempotencyKey={}, transactionRef={}", idempotencyKey,
							transactionReference);

					try {
						if (record.getResponseJson() == null) {

						    throw new PaymentProcessingException(
						            ErrorCode.PAYMENT_PROCESSING_ERROR,
						            "Cached response missing");
						}

						return objectMapper.readValue(record.getResponseJson(), TransactionResponse.class);

					} catch (JsonProcessingException ex) {

						throw new PaymentProcessingException(ErrorCode.PAYMENT_PROCESSING_ERROR,
								"Unable to deserialize cached response", ex);
					}

				case PROCESSING:

				    if (isProcessingExpired(record)) {

				        transactionReference =
				        		TRANSACTION_PREFIX+generateTransactionReference();

				        MDC.put(
				                MDC_TRANSACTION_REF,
				                transactionReference);

				        log.warn(
				                "Recovering stale PROCESSING record. idempotencyKey={}, transactionRef={}",
				                idempotencyKey,
				                transactionReference);

				        idempotencyService.resetToProcessing(
				                idempotencyKey,
				                transactionReference);

				        paymentOwnedByCurrentRequest = true;

				        break;
				    }

				    throw new PaymentProcessingException(
				            ErrorCode.PAYMENT_PROCESSING_ERROR,
				            "Payment already in progress");

				case FAILED:

					transactionReference = generateTransactionReference();

					MDC.put(MDC_TRANSACTION_REF, transactionReference);

					log.info("Retrying failed payment. idempotencyKey={}", idempotencyKey);

					idempotencyService.resetToProcessing(idempotencyKey, transactionReference);

					paymentOwnedByCurrentRequest = true;

					break;

				default:

					throw new PaymentProcessingException(ErrorCode.PAYMENT_PROCESSING_ERROR,
							"Invalid idempotency status");
				}

			} else {

				transactionReference = record.getTransactionReference();

				MDC.put(MDC_TRANSACTION_REF, transactionReference);
			}

			log.info(
					"Payment initiated. transactionRef={}, idempotencyKey={}, senderUserId={}, receiverUserId={}, amount={}",
					transactionReference, idempotencyKey, request.getSenderUserId(), request.getReceiverUserId(),
					request.getAmount());

			transaction = transactionService.createPendingTransaction(request, transactionReference);

			MDC.put(MDC_TRANSACTION_ID, String.valueOf(transaction.getId()));

			WalletTransferResponse walletResponse = walletFacadeService.transferMoney(request);

			TransactionResponse response = transactionMapper.toResponse(transactionReference, walletResponse,
					PaymentStatus.SUCCESS);

			String responseJson = serializeResponse(response, transactionReference);

			transactionService.markTransactionSuccess(transaction.getId(),
					walletResponse.getWalletTransactionReference());

			idempotencyService.markSuccess(idempotencyKey, responseJson);

			log.info("Payment completed successfully. transactionId={}, walletTxnRef={}", transaction.getId(),
					walletResponse.getWalletTransactionReference());

			return response;

		} catch (BadRequestException | PaymentProcessingException | IdempotencyRecordNotFoundException ex) {

			updateTransactionAsFailed(transaction);

			if (paymentOwnedByCurrentRequest) {

				idempotencyService.markFailed(idempotencyKey);
			}

			throw ex;

		} catch (Exception ex) {

			log.error("Unexpected payment processing failure. transactionRef={}", transactionReference, ex);

			updateTransactionAsFailed(transaction);

			if (paymentOwnedByCurrentRequest) {

				idempotencyService.markFailed(idempotencyKey);
			}

			throw new PaymentProcessingException(ErrorCode.PAYMENT_PROCESSING_ERROR,
					"Unexpected error occurred while processing payment", ex);
		} finally {

			MDC.remove(MDC_TRANSACTION_REF);

			MDC.remove(MDC_TRANSACTION_ID);
		}
	}

	private void updateTransactionAsFailed(Transaction transaction) {

		if (transaction == null) {
			return;
		}

		transactionService.safelyUpdateTransactionStatus(transaction.getId(), PaymentStatus.FAILED);
	}

	private void validateBusinessRules(TransferRequest request) {

		if (request == null) {
			throw new BadRequestException("Transfer request cannot be null");
		}

		if (request.getSenderUserId() == null) {
			throw new BadRequestException("Sender user id is mandatory");
		}

		if (request.getReceiverUserId() == null) {
			throw new BadRequestException("Receiver user id is mandatory");
		}

		if (request.getAmount() == null) {
			throw new BadRequestException("Amount is mandatory");
		}

		if (request.getAmount().signum() <= 0) {
			throw new BadRequestException("Amount must be greater than zero");
		}

		if (request.getSenderUserId().equals(request.getReceiverUserId())) {

			log.warn(SAME_USER_TRANSFER_ATTEMPT, request.getSenderUserId());

			throw new BadRequestException(SENDER_RECEIVER_SAME);
		}
	}

	private String generateTransactionReference() {

		String reference = TRANSACTION_PREFIX + referenceGenerator.generateReference(TRANSACTION_REFERENCE_LENGTH);

		log.debug("Generated transaction reference={}", reference);

		return reference;
	}

	private void validateIdempotencyKey(String idempotencyKey) {

		if (idempotencyKey == null || idempotencyKey.isBlank()) {

			throw new BadRequestException("Idempotency key is mandatory");
		}

		if (idempotencyKey.length() > 100) {

			throw new BadRequestException("Invalid idempotency key");
		}
	}

	private String serializeResponse(TransactionResponse response, String transactionReference) {

		try {

			return objectMapper.writeValueAsString(response);

		} catch (JsonProcessingException ex) {

			log.error("Failed to serialize payment response. transactionRef={}", transactionReference, ex);

			throw new PaymentProcessingException(ErrorCode.PAYMENT_PROCESSING_ERROR,
					"Unable to serialize payment response", ex);
		}
	}
	private boolean isProcessingExpired(
	        IdempotencyRecord record) {

	    if (record.getProcessingStartedAt() == null) {

	        return false;
	    }

	    return record.getProcessingStartedAt()
	            .plusMinutes(
	                    PROCESSING_TIMEOUT_MINUTES)
	            .isBefore(
	                    LocalDateTime.now());
	}
}
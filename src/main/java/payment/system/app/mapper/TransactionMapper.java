package payment.system.app.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import payment.system.app.dto.TransactionResponse;
import payment.system.app.dto.WalletTransferResponse;
import payment.system.app.enums.PaymentStatus;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(
            String transactionReference,
            WalletTransferResponse walletResponse,
            PaymentStatus status) {

    	return TransactionResponse.builder()
    	        .transactionReference(transactionReference)
    	        .walletTransactionReference(
    	                walletResponse.getWalletTransactionReference())
    	        .senderUserId(
    	                walletResponse.getSenderUserId())
    	        .receiverUserId(
    	                walletResponse.getReceiverUserId())
    	        .amount(
    	                walletResponse.getAmount())
    	        .status(
    	                status.name())
    	        .timestamp(
    	                LocalDateTime.now())
    	        .build();
    	
        }
    }

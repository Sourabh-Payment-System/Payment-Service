package payment.system.app.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import payment.system.app.dto.TransactionSearchRequest;
import payment.system.app.entity.Transaction;

public final class TransactionSpecification {

    private static final String SENDER_USER_ID = "senderUserId";
    private static final String RECEIVER_USER_ID = "receiverUserId";
    private static final String STATUS = "status";
    private static final String TRANSACTION_REFERENCE = "transactionReference";
    private static final String AMOUNT = "amount";
    private static final String CREATED_AT = "createdAt";

    private TransactionSpecification() {
    }

    public static Specification<Transaction> search(
            TransactionSearchRequest request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            addSenderPredicate(request, root, cb, predicates);
            addReceiverPredicate(request, root, cb, predicates);
            addStatusPredicate(request, root, cb, predicates);
            addReferencePredicate(request, root, cb, predicates);
            addAmountPredicate(request, root, cb, predicates);
            addDatePredicate(request, root, cb, predicates);


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addSenderPredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getSenderUserId() != null) {
            predicates.add(
                    cb.equal(
                            root.get(SENDER_USER_ID),
                            request.getSenderUserId()));
        }
    }

    private static void addReceiverPredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getReceiverUserId() != null) {
            predicates.add(
                    cb.equal(
                            root.get(RECEIVER_USER_ID),
                            request.getReceiverUserId()));
        }
    }

    private static void addStatusPredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getStatus() != null) {
            predicates.add(
                    cb.equal(
                            root.get(STATUS),
                            request.getStatus()));
        }
    }

    private static void addReferencePredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getTransactionReference() != null &&
                !request.getTransactionReference().isBlank()) {

            predicates.add(
                    cb.like(
                            cb.lower(root.get(TRANSACTION_REFERENCE)),
                            "%" + request.getTransactionReference()
                                    .toLowerCase()
                                    .trim() + "%"));
        }
    }

    private static void addAmountPredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getMinAmount() != null) {
            predicates.add(
                    cb.greaterThanOrEqualTo(
                            root.get(AMOUNT),
                            request.getMinAmount()));
        }

        if (request.getMaxAmount() != null) {
            predicates.add(
                    cb.lessThanOrEqualTo(
                            root.get(AMOUNT),
                            request.getMaxAmount()));
        }
    }

    private static void addDatePredicate(
            TransactionSearchRequest request,
            Root<Transaction> root,
            CriteriaBuilder cb,
            List<Predicate> predicates) {

        if (request.getFrom() != null) {
            predicates.add(
                    cb.greaterThanOrEqualTo(
                            root.get(CREATED_AT),
                            request.getFrom()));
        }

        if (request.getTo() != null) {
            predicates.add(
                    cb.lessThanOrEqualTo(
                            root.get(CREATED_AT),
                            request.getTo()));
        }
    }

}
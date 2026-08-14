package payment.system.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import payment.system.app.entity.Transaction;

@Repository
public interface TransactionRepository extends
        JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.id = :id
            """)
    Optional<Transaction> findByIdForUpdate(
            @Param("id") Long id);

}
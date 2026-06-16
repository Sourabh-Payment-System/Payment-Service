package payment.system.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import payment.system.app.entity.Transaction;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select t
           from Transaction t
           where t.id = :id
           """)
    Optional<Transaction> findByIdForUpdate(
            @Param("id") Long id);
}
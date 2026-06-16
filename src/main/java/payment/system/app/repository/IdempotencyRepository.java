package payment.system.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import payment.system.app.entity.IdempotencyRecord;

@Repository
public interface IdempotencyRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(
            String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select i
            from IdempotencyRecord i
            where i.idempotencyKey = :idempotencyKey
            """)
    Optional<IdempotencyRecord> findByIdempotencyKeyForUpdate(
            @Param("idempotencyKey")
            String idempotencyKey);


    @Modifying
    @Query(
            value = """
                    INSERT INTO payment_db.idempotency_records
                    (
                        idempotency_key,
                        transaction_reference,
                        status,
                        response_json,
                        created_at,
                        updated_at,
                        version
                    )
                    VALUES
                    (
                        :idempotencyKey,
                        :transactionReference,
                        'PROCESSING',
                        NULL,
                        NOW(),
                        NOW(),
                        0
                    )
                    ON CONFLICT (idempotency_key)
                    DO NOTHING
                    """,
            nativeQuery = true)
    int insertIfAbsent(
            @Param("idempotencyKey")
            String idempotencyKey,
            @Param("transactionReference")
            String transactionReference);
}
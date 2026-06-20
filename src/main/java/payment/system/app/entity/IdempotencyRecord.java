package payment.system.app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import lombok.*;
import payment.system.app.enums.IdempotencyStatus;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idempotency_key",
                        columnNames = "idempotency_key")
        },
        indexes = {
                @Index(
                        name = "idx_idempotency_status",
                        columnList = "status"),
                @Index(
                        name = "idx_processing_started_at",
                        columnList = "processing_started_at"),
                @Index(
                        name = "idx_completed_at",
                        columnList = "completed_at")
        })
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100)
    private String idempotencyKey;

    @Column(
            name = "transaction_reference",
            nullable = false)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Lob
    @Column(name = "response_json")
    private String responseJson;
    
    
    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;
    

    @CreatedDate
    @Column(
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;}
    
   
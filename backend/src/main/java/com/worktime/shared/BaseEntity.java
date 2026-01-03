package com.worktime.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Base entity class providing common fields for all entities.
 * Includes UUID primary key and audit metadata.
 *
 * @author Thang
 * @since 2026-01-02
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.isDeleted = false;
        // TODO: Set createdBy from security context when authentication is implemented
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        // TODO: Set updatedBy from security context when authentication is implemented
    }

    /**
     * Soft delete this entity.
     */
    public void softDelete() {
        this.isDeleted = true;
    }

    /**
     * Check if this entity is soft deleted.
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted;
    }
}
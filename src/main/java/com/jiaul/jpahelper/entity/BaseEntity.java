package com.jiaul.jpahelper.entity;

import com.jiaul.jpahelper.hibernate.DeleteFilter;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base abstract mapped superclass providing auditing and soft-delete capabilities.
 * <p>
 * Subclass entities automatically inherit:
 * <ul>
 *   <li>Audit timestamps: {@code createdAt} and {@code updatedAt}</li>
 *   <li>Auditor tracking: {@code createdBy} and {@code updatedBy} (populated via Spring Data Auditing)</li>
 *   <li>Soft-delete status and timestamp: {@code deleted} and {@code deletedAt}</li>
 *   <li>Hibernate soft-delete filter: Pre-bound to {@link DeleteFilter#FILTER}</li>
 * </ul>
 * <p>
 * If Spring Data Auditing is not enabled or an auditor is absent, lifecycle hooks
 * ({@link #onPrePersist()} and {@link #onPreUpdate()}) automatically populate safe defaults
 * ({@code "SYSTEM"} and {@link LocalDateTime#now()}).
 */
@MappedSuperclass
@FilterDef(name = DeleteFilter.FILTER, parameters = @ParamDef(name = DeleteFilter.PARAMETER, type = Boolean.class))
@Filter(name = DeleteFilter.FILTER, condition = DeleteFilter.CONDITION)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * Timestamp when the entity was first created and persisted.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Identifier or username of the user who created the entity.
     */
    @CreatedBy
    @Column(nullable = false, updatable = false, length = 320)
    private String createdBy;

    /**
     * Timestamp when the entity was last updated.
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Identifier or username of the user who last modified the entity.
     */
    @LastModifiedBy
    @Column(nullable = false, length = 320)
    private String updatedBy;

    /**
     * Timestamp when the entity was marked as soft-deleted; {@code null} if active.
     */
    private LocalDateTime deletedAt;

    /**
     * Soft-delete flag; {@code false} for active records, {@code true} for deleted records.
     */
    @Column(nullable = false)
    private boolean deleted;

    // -------------------------------------------------------------
    // Lifecycle Callbacks
    // -------------------------------------------------------------

    /**
     * Fallback callback executed before entity persistence.
     * <p>
     * Ensures audit timestamps and usernames are populated with safe defaults
     * if Spring Data Auditing does not supply them.
     */
    @PrePersist
    protected void onPrePersist() {
        if (this.createdBy == null) {
            this.createdBy = "SYSTEM";
        }
        if (this.updatedBy == null) {
            this.updatedBy = "SYSTEM";
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Fallback callback executed before entity update.
     * <p>
     * Ensures modification timestamp and username are populated with safe defaults
     * if Spring Data Auditing does not supply them.
     */
    @PreUpdate
    protected void onPreUpdate() {
        if (this.updatedBy == null) {
            this.updatedBy = "SYSTEM";
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    // -------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------

    /**
     * Returns the user or principal who created this entity.
     *
     * @return the creator identifier
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the creator identifier.
     *
     * @param createdBy the username or system identifier
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the timestamp when this entity was persisted.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the timestamp when this entity was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the user or principal who last modified this entity.
     *
     * @return the updater identifier
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets the updater identifier.
     *
     * @param updatedBy the username or system identifier
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Returns the soft-delete timestamp.
     *
     * @return the deletion timestamp, or {@code null} if active
     */
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    /**
     * Sets the soft-delete timestamp.
     *
     * @param deletedAt the timestamp when the entity was soft-deleted
     */
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Checks whether this entity is soft-deleted.
     *
     * @return {@code true} if marked as deleted, {@code false} if active
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets the soft-delete flag.
     *
     * @param deleted {@code true} to mark as deleted, {@code false} to mark as active
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}

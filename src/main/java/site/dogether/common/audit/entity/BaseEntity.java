package site.dogether.common.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@MappedSuperclass
public class BaseEntity {

    @Column(name = "row_inserted_at", nullable = false, updatable = false)
    private LocalDateTime rowInsertedAt;

    @Column(name = "row_updated_at")
    private LocalDateTime rowUpdatedAt;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    protected boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        this.rowInsertedAt = LocalDateTime.now();
        this.rowUpdatedAt = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.rowUpdatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}

package com.postest.domain.entities;

import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.domain.enums.TerminalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "terminal_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TerminalType terminalType;

    @Embedded
    @Column(nullable = false)
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TerminalRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UUID reservedTerminalId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}


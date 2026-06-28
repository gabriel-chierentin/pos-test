package com.postest.domain.entities;

import com.postest.domain.enums.TerminalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "terminals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terminal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TerminalType terminalType;

    @Column(nullable = false)
    private Boolean isAvailable;

    private String reservedBy;
}


package com.insurance.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contracts", indexes = {
        @Index(name = "idx_client_id", columnList = "client_id"),
        @Index(name = "idx_end_date", columnList = "end_date"),
        @Index(name = "idx_update_date", columnList = "update_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal costAmount;

    @Column(nullable = false)
    @JsonIgnore
    private LocalDate updateDate;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        updateDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDate.now();
    }
}
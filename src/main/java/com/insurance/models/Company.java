package com.insurance.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("COMPANY")
@Data
@NoArgsConstructor
public class Company extends Client {
    @Column(nullable = true, unique = true, updatable = false)
    private String companyIdentifier;

    public Company(Long id, String name, String email, String phone, String companyIdentifier) {
        super(id, name, email, phone, null);
        this.companyIdentifier = companyIdentifier;
    }
}
package com.insurance.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PersonDTO.class, name = "PERSON"),
        @JsonSubTypes.Type(value = CompanyDTO.class, name = "COMPANY")
})
public sealed interface ClientDTO permits PersonDTO, CompanyDTO {
    Long id();
    String name();
    String email();
    String phone();
}


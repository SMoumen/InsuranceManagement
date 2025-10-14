package com.insurance.repository;

import com.insurance.models.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId " +
            "AND (c.endDate IS NULL OR c.endDate > :currentDate)")
    List<Contract> findActiveContractsByClientId(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate
    );

    @Query("SELECT c FROM Contract c WHERE c.client.id = :clientId " +
            "AND (c.endDate IS NULL OR c.endDate > :currentDate) " +
            "AND c.updateDate = :updateDate")
    List<Contract> findActiveContractsByClientIdAndUpdateDate(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate,
            @Param("updateDate") LocalDate updateDate
    );

    @Query("SELECT COALESCE(SUM(c.costAmount), 0) FROM Contract c " +
            "WHERE c.client.id = :clientId " +
            "AND (c.endDate IS NULL OR c.endDate > :currentDate)")
    BigDecimal sumActiveContractsCostByClientId(
            @Param("clientId") Long clientId,
            @Param("currentDate") LocalDate currentDate
    );

    List<Contract> findByClientId(Long clientId);
}

package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.dto.DonationResponseDto;
import com.iskcondhanbad.donordash.model.Donation;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT d FROM Donation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo)")
    List<Donation> findAllDonationsByCultivatorInGivenRange(@Param("cultivatorId") Integer cultivatorId,
                                    @Param("dateFrom") Date dateFrom,
                                    @Param("dateTo") Date dateTo);

    @Query("SELECT new com.iskcondhanbad.donordash.dto.DonationResponseDto(" +
            "  d.id, " +                       // Long id
            "  donor.name, " +                 // String donorName
            "  d.amount, " +                   // Double amount
            "  d.paymentDate, " +              // Date paymentDatDonationResponseDtoe  (typo name in DTO)
            "  d.purpose, " +                  // String purpose
            "  d.paymentMode, " +              // String paymentMode
            "  d.transactionId, " +            // String transactionId
            "  d.status, " +                   // String status
            "  d.remark, " +                   // String remark
            "  donor.id, " +                   // Integer donorId
            "  d.createdAt, " +                // Date createdAt
            "  d.paymentDate, " +              // Date paymentDate (DTO has this field as well)
            "  d.receiptId, " +                // String receiptId
            "  d.verifiedAt, " +               // Date verifiedAt
            "  dc.name, " +                    // String donorCultivatorName
            "  collectedBy.name " +            // String collectedByName
            ") " +
            "FROM Donation d " +
            "LEFT JOIN d.donor donor " +
            "LEFT JOIN donor.donorCultivator dc " +
            "LEFT JOIN d.collectedBy collectedBy " +
            "ORDER BY d.createdAt DESC")
    List<DonationResponseDto> findAllDonationDtos();



    Optional<Donation> findByTransactionId(String transactionId);

    @Query("SELECT d.donor.zone, SUM(d.amount) FROM Donation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.donor.zone " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByZone(@Param("cultivatorId") Integer cultivatorId,
                                         @Param("dateFrom") Date dateFrom,
                                         @Param("dateTo") Date dateTo);

    @Query("SELECT d.purpose, SUM(d.amount) FROM Donation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.purpose " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByPurpose(@Param("cultivatorId") Integer cultivatorId,
                                            @Param("dateFrom") Date dateFrom,
                                            @Param("dateTo") Date dateTo);

    List<Donation> findByDonorId(Integer donorId);

    @Query("SELECT d.paymentMode, SUM(d.amount) FROM Donation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.paymentMode " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByPaymentMode(@Param("cultivatorId") Integer cultivatorId,
                                                @Param("dateFrom") Date dateFrom,
                                                @Param("dateTo") Date dateTo);

    @Query("SELECT dc.name, SUM(d.amount) FROM Donation d " +
           "JOIN d.collectedBy dc " +
           "WHERE (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY dc.id, dc.name " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByCultivator(@Param("dateFrom") Date dateFrom,
                                               @Param("dateTo") Date dateTo);
}

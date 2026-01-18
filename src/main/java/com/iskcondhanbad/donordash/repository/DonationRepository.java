package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.dto.Donation;
import com.iskcondhanbad.donordash.model.StoredDonation;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface DonationRepository extends JpaRepository<StoredDonation, Long> {

    @Query("SELECT d FROM StoredDonation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo)")
    List<StoredDonation> findAllDonationsByCultivatorInGivenRange(@Param("cultivatorId") Integer cultivatorId,
                                    @Param("dateFrom") Date dateFrom,
                                    @Param("dateTo") Date dateTo);

    @Query(
            "SELECT new com.iskcondhanbad.donordash.dto.Donation(" +
                    "  sd.id, " +                    // Long id
                    "  d.name, " +                   // String donorName
                    "  sd.amount, " +                // Double amount
                    "  sd.purpose, " +               // String purpose
                    "  sd.paymentMode, " +           // String paymentMode
                    "  sd.transactionId, " +         // String transactionId
                    "  sd.status, " +                // String status
                    "  sd.remark, " +                // String remark
                    "  d.id, " +                     // Integer donorId
                    "  sd.createdAt, " +             // Date createdAt
                    "  sd.receiptId, " +             // String receiptId
                    "  sd.verifiedAt, " +            // Date verifiedAt
                    "  sd.paymentDate, " +           // Date paymentDate
                    "  cb.name, " +                  // String collectedByName
                    "  dc.name " +                   // String donorCultivatorName
                    ") " +
                    "FROM StoredDonation sd " +
                    "JOIN sd.donor d " +
                    "LEFT JOIN sd.collectedBy cb " +
                    "LEFT JOIN d.donorCultivator dc " +
                    "LEFT JOIN dc.donationSupervisor ds " +
                    "WHERE sd.paymentDate BETWEEN :fromDate AND :toDate " +
                    "AND (:donorCultivatorId IS NULL OR dc.id = :donorCultivatorId) " +
                    "AND (:donationSupervisorId IS NULL OR ds.id = :donationSupervisorId) " +
                    "AND (:status IS NULL OR sd.status = :status) " +
                    "AND (:collectedById IS NULL OR cb.id = :collectedById) " +
                    "AND (:paymentMode IS NULL OR sd.paymentMode = :paymentMode) " +
                    "AND (:donorId IS NULL OR d.id = :donorId) " +
                    "AND (:donorIds IS NULL OR d.id IN :donorIds) " +
                    "AND (:minAmount IS NULL OR sd.amount >= :minAmount) " +
                    "AND (:maxAmount IS NULL OR sd.amount <= :maxAmount) " +
                    "ORDER BY sd.paymentDate DESC, sd.createdAt DESC"
    )
    List<Donation> findDonationsByFilter(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate,
            @Param("donorCultivatorId") Integer donorCultivatorId,
            @Param("donationSupervisorId") Integer donationSupervisorId,
            @Param("status") String status,
            @Param("collectedById") Integer collectedById,
            @Param("paymentMode") String paymentMode,
            @Param("donorId") Integer donorId,
            @Param("donorIds") List<Integer> donorIds,
            @Param("minAmount") Double minAmount,
            @Param("maxAmount") Double maxAmount
    );


    Optional<StoredDonation> findByTransactionId(String transactionId);

    @Query("SELECT d.donor.zone, SUM(d.amount) FROM StoredDonation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.donor.zone " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByZone(@Param("cultivatorId") Integer cultivatorId,
                                         @Param("dateFrom") Date dateFrom,
                                         @Param("dateTo") Date dateTo);

    @Query("SELECT d.purpose, SUM(d.amount) FROM StoredDonation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.purpose " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByPurpose(@Param("cultivatorId") Integer cultivatorId,
                                            @Param("dateFrom") Date dateFrom,
                                            @Param("dateTo") Date dateTo);

    List<StoredDonation> findByDonorId(Integer donorId);

    @Query("SELECT d.paymentMode, SUM(d.amount) FROM StoredDonation d " +
           "WHERE (:cultivatorId IS NULL OR d.collectedBy.id = :cultivatorId) " +
           "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY d.paymentMode " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByPaymentMode(@Param("cultivatorId") Integer cultivatorId,
                                                @Param("dateFrom") Date dateFrom,
                                                @Param("dateTo") Date dateTo);

    @Query("SELECT dc.name, SUM(d.amount) FROM StoredDonation d " +
           "JOIN d.collectedBy dc " +
           "WHERE (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
           "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
           "AND d.status = 'Verified' " +
           "GROUP BY dc.id, dc.name " +
           "ORDER BY SUM(d.amount) DESC")
    List<Object[]> findDonationSumByCultivator(@Param("dateFrom") Date dateFrom,
                                               @Param("dateTo") Date dateTo);
}

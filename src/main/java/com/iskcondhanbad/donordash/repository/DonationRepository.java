package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.model.Donation;

import java.util.List;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

        

        @Query("SELECT d FROM Donation d JOIN d.donor donor " +
                        "WHERE (:cultivatorId IS NULL OR donor.donorCultivator.id = :cultivatorId) " +
                        "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) ")
        List<Donation> findAllDonations(@Param("cultivatorId") Integer cultivatorId,
                        @Param("dateFrom") Date dateFrom,
                        @Param("dateTo") Date dateTo);

        @Query("SELECT donor.zone, SUM(d.amount) FROM Donation d JOIN d.donor donor " +
                        "WHERE (:cultivatorId IS NULL OR donor.donorCultivator.id = :cultivatorId) " +
                        "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
                        "AND (d.status = 'Verified') " +
                        "GROUP BY donor.zone " +
                        "ORDER BY SUM(d.amount) DESC ")
        List<Object[]> findDonationSumByZone(@Param("cultivatorId") Integer cultivatorId,
                        @Param("dateFrom") Date dateFrom,
                        @Param("dateTo") Date dateTo);

        @Query("SELECT d.purpose, SUM(d.amount) FROM Donation d JOIN d.donor donor " +
                        "WHERE (:cultivatorId IS NULL OR donor.donorCultivator.id = :cultivatorId) " +
                        "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
                        "AND (d.status = 'Verified') " +
                        "GROUP BY d.purpose " + 
                         "ORDER BY SUM(d.amount) DESC ")
        List<Object[]> findDonationSumByPurpose(@Param("cultivatorId") Integer cultivatorId,
                        @Param("dateFrom") Date dateFrom,
                        @Param("dateTo") Date dateTo);

        List<Donation> findByDonorId(Integer donorId);

        @Query("SELECT d.paymentMode, SUM(d.amount) FROM Donation d JOIN d.donor donor " +
                        "WHERE (:cultivatorId IS NULL OR donor.donorCultivator.id = :cultivatorId) " +
                        "AND (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
                        "AND (d.status = 'Verified') " +
                        "GROUP BY d.paymentMode " +
                        "ORDER BY SUM(d.amount) DESC ")
        List<Object[]> findDonationSumByPaymentMode(@Param("cultivatorId") Integer cultivatorId,
                        @Param("dateFrom") Date dateFrom,
                        @Param("dateTo") Date dateTo);

        @Query("SELECT dc.name, SUM(d.amount) " +
                        "FROM Donation d " +
                        "JOIN d.donor dr " +
                        "JOIN dr.donorCultivator dc " +
                        "WHERE (:dateFrom IS NULL OR d.createdAt >= :dateFrom) " +
                        "AND (:dateTo IS NULL OR d.createdAt <= :dateTo) " +
                        "AND (d.status = 'Verified') " +
                        "GROUP BY dc.id, dc.name " +
                        "ORDER BY SUM(d.amount) DESC ")
        List<Object[]> findDonationSumByCultivator(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo);

}

package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.dto.DonorDTO;
import com.iskcondhanbad.donordash.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Integer> {

       Donor findByMobileNumber(String mobileNumber);

       @Query("SELECT d FROM Donor d WHERE (:cultivatorNames IS NULL OR d.donorCultivator.name IN :cultivatorNames)")
       List<Donor> findByCultivatorNames(@Param("cultivatorNames") List<String> cultivatorNames);

       List<Donor> findByDonorCultivatorId(Integer cultivatorId);

       // Projection: Get selected donor details by cultivatorId
       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(" +
                     "d.id, d.name, d.username, d.category, d.photoPath) " +
                     "FROM Donor d " +
                     "WHERE d.donorCultivator.id = :cultivatorId")
       List<DonorDTO> findDonorsByCultivatorId(@Param("cultivatorId") Integer cultivatorId);

       // Projection: Get all donors with selected fields
       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(" +
                     "d.id, d.name, d.username, d.category, d.photoPath) " +
                     "FROM Donor d")
       List<DonorDTO> findAllDonors();

       // Get all donors by type (e.g., "Nitya Sevak")
       @Query("SELECT d FROM Donor d WHERE d.type = :type")
       List<Donor> findByType(@Param("type") String type);

       // Get donors by type and cultivatorId
       @Query("SELECT d FROM Donor d WHERE d.type = :type AND d.donorCultivator.id = :cultivatorId")
       List<Donor> findByTypeAndCultivatorId(@Param("type") String type,
                     @Param("cultivatorId") Integer cultivatorId);
}

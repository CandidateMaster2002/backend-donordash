package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.dto.DonorDTO;
import com.iskcondhanbad.donordash.dto.DonorDetailsDto;
import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.DonorTransfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Integer> {

       Donor findByMobileNumber(String mobileNumber);



       @Query("SELECT d FROM Donor d WHERE (:cultivatorNames IS NULL OR d.donorCultivator.name IN :cultivatorNames)")
       List<Donor> findByCultivatorNames(@Param("cultivatorNames") List<String> cultivatorNames);

       List<Donor> findByDonorCultivatorId(Integer cultivatorId);

       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDetailsDto(" +
       "d.id, d.name, CONCAT(d.address, ', ', d.city, ', ', d.state, ', ', d.pincode), " +
       "d.mobileNumber, d.email, d.panNumber, dc.name, d.zone, d.password, d.category, d.type, d.username, d.remark) " +
       "FROM Donor d JOIN d.donorCultivator dc WHERE dc.id = :cultivatorId")
List<DonorDetailsDto> findAllByDonorCultivatorId(@Param("cultivatorId") Integer cultivatorId);


@Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDetailsDto(" +
       "d.id, d.name, CONCAT(d.address, ', ', d.city, ', ', d.state, ', ', d.pincode), " +
       "d.mobileNumber, d.email, d.panNumber, dc.name, d.zone, d.password, d.category, d.type, d.username, d.remark) " +
       "FROM Donor d JOIN d.donorCultivator dc")
List<DonorDetailsDto> findAllDonorDetails();

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

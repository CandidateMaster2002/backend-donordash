package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.dto.Donor;
import com.iskcondhanbad.donordash.dto.DonorDTO;
import com.iskcondhanbad.donordash.dto.DonorDetailsDto;
import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.model.DonorTransfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<StoredDonor, Integer> {

       StoredDonor findByMobileNumber(String mobileNumber);



       @Query("SELECT d FROM StoredDonor d WHERE (:cultivatorNames IS NULL OR d.donorCultivator.name IN :cultivatorNames)")
       List<StoredDonor> findByCultivatorNames(@Param("cultivatorNames") List<String> cultivatorNames);

       List<StoredDonor> findByDonorCultivatorId(Integer cultivatorId);

       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDetailsDto(" +
       "d.id, d.name, CONCAT(d.address, ', ', d.city, ', ', d.state, ', ', d.pincode), " +
       "d.mobileNumber, d.email, d.panNumber, dc.name, d.zone, d.password, d.category, d.type, d.username, d.remark) " +
       "FROM StoredDonor d JOIN d.donorCultivator dc WHERE dc.id = :cultivatorId")
List<DonorDetailsDto> findAllByDonorCultivatorId(@Param("cultivatorId") Integer cultivatorId);


@Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDetailsDto(" +
       "d.id, d.name, CONCAT(d.address, ', ', d.city, ', ', d.state, ', ', d.pincode), " +
       "d.mobileNumber, d.email, d.panNumber, dc.name, d.zone, d.password, d.category, d.type, d.username, d.remark) " +
       "FROM StoredDonor d JOIN d.donorCultivator dc")
List<DonorDetailsDto> findAllDonorDetails();

       // Projection: Get selected donor details by cultivatorId
       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(" +
                     "d.id, d.name, d.username, d.category, d.photoPath) " +
                     "FROM StoredDonor d " +
                     "WHERE d.donorCultivator.id = :cultivatorId")
       List<DonorDTO> findDonorsByCultivatorId(@Param("cultivatorId") Integer cultivatorId);

       // Projection: Get all donors with selected fields
       @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(" +
                     "d.id, d.name, d.username, d.category, d.photoPath) " +
                     "FROM StoredDonor d")
       List<DonorDTO> findAllDonors();

       // Get all donors by type (e.g., "Nitya Sevak")
       @Query("SELECT d FROM StoredDonor d WHERE d.type = :type")
       List<StoredDonor> findByType(@Param("type") String type);

       // Get donors by type and cultivatorId
       @Query("SELECT d FROM StoredDonor d WHERE d.type = :type AND d.donorCultivator.id = :cultivatorId")
       List<StoredDonor> findByTypeAndCultivatorId(@Param("type") String type,
                     @Param("cultivatorId") Integer cultivatorId);

    @Query("""
                select new com.iskcondhanbad.donordash.dto.Donor(
                    d.id,
                    d.name,
                    d.category,
                    d.mobileNumber,
                    d.email,
                    d.address,
                    d.city,
                    d.state,
                    d.pincode,
                    d.panNumber,
                    c.name,
                    d.zone,
                    d.remark,
                    d.type,
                    d.username
                )
                from StoredDonor d
                left join d.donorCultivator c
                where d.id = :id
            """)
    Optional<Donor> findDonorById(@Param("id") Integer id);


    @Query("""
                select new com.iskcondhanbad.donordash.dto.Donor(
                    d.id,
                    d.name,
                    d.category,
                    d.mobileNumber,
                    d.email,
                    d.address,
                    d.city,
                    d.state,
                    d.pincode,
                    d.panNumber,
                    c.name,
                    d.zone,
                    d.remark,
                    d.type,
                    d.username
                )
                from StoredDonor d
                left join d.donorCultivator c
                where (:cultivatorId is null or c.id = :cultivatorId)
            """)
    List<Donor> findDonors(@Param("cultivatorId") Integer cultivatorId);

}

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

    Donor findByEmail(String email);
    List<Donor> findByDonorCultivatorId(Integer cultivatorId);

   @Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(d.id, d.name, d.username, d.category, d.photoPath) " +
       "FROM Donor d JOIN d.donorCultivator c " +
       "WHERE c.id = :cultivatorId")
List<DonorDTO> findDonorsByCultivatorId(@Param("cultivatorId") Integer cultivatorId);

@Query("SELECT new com.iskcondhanbad.donordash.dto.DonorDTO(d.id, d.name, d.username, d.category, d.photoPath) " +
       "FROM Donor d")
List<DonorDTO> findAllDonors();
}


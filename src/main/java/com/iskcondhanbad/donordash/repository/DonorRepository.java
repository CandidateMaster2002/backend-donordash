package com.iskcondhanbad.donordash.repository;
import com.iskcondhanbad.donordash.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Integer> {

    Donor findByEmail(String email);
    List<Donor> findByDonorCultivatorId(Integer cultivatorId);
}


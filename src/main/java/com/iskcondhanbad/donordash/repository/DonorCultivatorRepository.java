package com.iskcondhanbad.donordash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import java.util.*;

public interface DonorCultivatorRepository extends JpaRepository<DonorCultivator, Integer>{

    DonorCultivator findByEmail(String email);
    List<DonorCultivator> findByDonationSupervisorId(Integer supervisorId);

}

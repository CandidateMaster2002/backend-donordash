package com.iskcondhanbad.donordash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import java.util.*;

public interface DonorCultivatorRepository extends JpaRepository<DonorCultivator, Integer>{

    DonorCultivator findByMobileNumber(String mobileNumber);
    List<DonorCultivator> findByDonationSupervisorId(Integer supervisorId);

}

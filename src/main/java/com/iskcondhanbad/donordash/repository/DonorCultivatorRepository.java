package com.iskcondhanbad.donordash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.iskcondhanbad.donordash.model.StoredDonorCultivator;
import java.util.*;

public interface DonorCultivatorRepository extends JpaRepository<StoredDonorCultivator, Integer>{

    StoredDonorCultivator findByMobileNumber(String mobileNumber);
    List<StoredDonorCultivator> findByDonationSupervisorId(Integer supervisorId);

}

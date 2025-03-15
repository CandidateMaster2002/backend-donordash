package com.iskcondhanbad.donordash.repository;
import com.iskcondhanbad.donordash.model.DonationSupervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationSupervisorRepository extends JpaRepository<DonationSupervisor, Integer> {

    DonationSupervisor findByEmail(String email);

    
}


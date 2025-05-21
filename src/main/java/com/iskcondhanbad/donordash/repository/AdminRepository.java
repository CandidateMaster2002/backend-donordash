package com.iskcondhanbad.donordash.repository;
import com.iskcondhanbad.donordash.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Admin findByMobileNumber(String mobileNumber);
}


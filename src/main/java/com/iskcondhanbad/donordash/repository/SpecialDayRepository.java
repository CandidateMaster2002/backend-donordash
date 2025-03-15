package com.iskcondhanbad.donordash.repository;
import com.iskcondhanbad.donordash.model.SpecialDay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Integer> {


    List<SpecialDay> findAllByDonorId(Integer donorId);
}


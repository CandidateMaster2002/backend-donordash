package com.iskcondhanbad.donordash.repository;
import com.iskcondhanbad.donordash.model.NityaSevaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NityaSevaStatusRepository extends JpaRepository<NityaSevaStatus, Long> {

    
     @Query("SELECT s FROM NityaSevaStatus s WHERE s.donor.id = :donorId AND s.month = :month")
    Optional<NityaSevaStatus> findByDonorIdAndMonth(Integer donorId, String month);
}

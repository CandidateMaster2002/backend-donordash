package com.iskcondhanbad.donordash.repository;

import com.iskcondhanbad.donordash.model.DonorTransfer;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DonorTransferRepository extends JpaRepository<DonorTransfer, Integer> {

        Optional<DonorTransfer> findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(
        Integer donorId, Integer requestedById, Integer requestedToId, String requestType);

        Optional<DonorTransfer> findByDonorIdAndRequestedByIdAndRequestType(Integer donorId, Integer requestedById,
            String string);


        @Query("SELECT dt FROM DonorTransfer dt WHERE dt.requestedTo.id = :cultivatorId OR dt.requestedBy.id = :cultivatorId")
    List<DonorTransfer> findAllPendingByCultivatorId(Integer cultivatorId);
    
}

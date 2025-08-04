package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.model.DonorTransfer;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.DonorTransferRepository;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DonorTransferService {

    @Autowired
    DonorRepository donorRepository;

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @Autowired
    DonorTransferRepository donorTransferRepository;


    public boolean isDuplicateTransferRequest(Integer donorId, Integer requestedById, Integer requestedToId, String requestType) {
        return donorTransferRepository
                .findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(donorId, requestedById, requestedToId, requestType)
                .isPresent();
    }

    public DonorTransfer requestToAcquire(Integer donorId, Integer requestedById) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if(donorTransferRepository.findByDonorIdAndRequestedByIdAndRequestType(donorId, requestedById, "ACQUIRE").isPresent()) {
            throw new RuntimeException("A similar acquire request already exists");
        }
        DonorCultivator requestedBy = donorCultivatorRepository.findById(requestedById)
                .orElseThrow(() -> new RuntimeException("Requesting cultivator not found"));

        DonorTransfer transfer = new DonorTransfer();
        transfer.setDonor(donor);
        transfer.setRequestedBy(requestedBy);
        transfer.setRequestedTo(donor.getDonorCultivator());
        transfer.setRequestType("ACQUIRE");

        donorTransferRepository.save(transfer);
        return transfer;
    }
 
    public DonorTransfer requestToRelease(Integer donorId, Integer fromId, Integer toId) {

if(donorTransferRepository.findByDonorIdAndRequestedByIdAndRequestType(donorId, fromId, "RELEASE").isPresent()) {
            throw new RuntimeException("A similar release request already exists");
        }

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        DonorCultivator from = donorCultivatorRepository.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Requesting cultivator not found"));
        
        DonorCultivator to = donorCultivatorRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Target cultivator not found"));

        DonorTransfer transfer = new DonorTransfer();
        transfer.setDonor(donor);
        transfer.setRequestedBy(from);
        transfer.setRequestedTo(to);
        transfer.setRequestType("RELEASE");

        donorTransferRepository.save(transfer);
        return transfer;
    }

    public DonorTransfer approveAcquire(Integer donorId, Integer fromId, Integer toId) {
        Optional<DonorTransfer> transferOpt = donorTransferRepository
                .findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(
                        donorId, fromId, toId, "ACQUIRE");

        if (transferOpt.isEmpty())
            throw new RuntimeException("No matching acquire request found");

        DonorTransfer transfer = transferOpt.get();
        Donor donor = transfer.getDonor();

        donor.setDonorCultivator(transfer.getRequestedBy()); // requestedBy is new owner
        donorRepository.save(donor);
        donorTransferRepository.delete(transfer);

        return transfer;
    }

    public DonorTransfer approveRelease(Integer donorId, Integer fromId, Integer toId) {
        Optional<DonorTransfer> transferOpt = donorTransferRepository
                .findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(
                        donorId, fromId, toId, "RELEASE");

        if (transferOpt.isEmpty())
            throw new RuntimeException("No matching release request found");

        DonorTransfer transfer = transferOpt.get();
        Donor donor = transfer.getDonor();

        donor.setDonorCultivator(transfer.getRequestedTo()); // requestedTo is new owner
        donorRepository.save(donor);
        donorTransferRepository.delete(transfer);

        return transfer;
    }

    // DonorTransferService.java

    public List<DonorTransfer> getPendingRequestsByCultivator(Integer cultivatorId) {
        return donorTransferRepository.findAllPendingByCultivatorId(cultivatorId);
    }

}
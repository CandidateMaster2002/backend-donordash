package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.model.StoredDonorCultivator;
import com.iskcondhanbad.donordash.model.DonorTransfer;
import com.iskcondhanbad.donordash.dto.DonorTransferDto;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.DonorTransferRepository;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class DonorTransferService {

    @Autowired
    DonorRepository donorRepository;

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @Autowired
    DonorTransferRepository donorTransferRepository;

    public DonorTransferDto requestToAcquire(Integer donorId, Integer requestedById) {
        StoredDonor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));

        if(donorTransferRepository.findByDonorIdAndRequestedByIdAndRequestType(donorId, requestedById, "ACQUIRE").isPresent()) {
            throw new IllegalArgumentException("A similar acquire request already exists");
        }
        StoredDonorCultivator requestedBy = donorCultivatorRepository.findById(requestedById)
                .orElseThrow(() -> new IllegalArgumentException("Requesting cultivator not found"));

        DonorTransfer transfer = new DonorTransfer();
        transfer.setDonor(donor);
        transfer.setRequestedBy(requestedBy);
        transfer.setRequestedTo(donor.getDonorCultivator());
        transfer.setRequestType("ACQUIRE");

        donorTransferRepository.save(transfer);
        return convertToDto(transfer);
    }

    public DonorTransferDto requestToRelease(Integer donorId, Integer fromId, Integer toId) {

        if(donorTransferRepository.findByDonorIdAndRequestedByIdAndRequestType(donorId, fromId, "RELEASE").isPresent()) {
            throw new IllegalArgumentException("A similar release request already exists");
        }

        StoredDonor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));
        StoredDonorCultivator from = donorCultivatorRepository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("Requesting cultivator not found"));

        StoredDonorCultivator to = donorCultivatorRepository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("Target cultivator not found"));

        DonorTransfer transfer = new DonorTransfer();
        transfer.setDonor(donor);
        transfer.setRequestedBy(from);
        transfer.setRequestedTo(to);
        transfer.setRequestType("RELEASE");

        donorTransferRepository.save(transfer);
        return convertToDto(transfer);
    }

    public DonorTransferDto approveAcquire(Integer donorId, Integer fromId, Integer toId) {
        Optional<DonorTransfer> transferOpt = donorTransferRepository
                .findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(
                        donorId, fromId, toId, "ACQUIRE");

        if (transferOpt.isEmpty())
            throw new IllegalArgumentException("No matching acquire request found");

        DonorTransfer transfer = transferOpt.get();
        StoredDonor donor = transfer.getDonor();

        donor.setDonorCultivator(transfer.getRequestedBy()); // requestedBy is new owner
        donorRepository.save(donor);
        donorTransferRepository.delete(transfer);

        return convertToDto(transfer);
    }


    public DonorTransferDto approveRelease(Integer donorId, Integer fromId, Integer toId) {
        Optional<DonorTransfer> transferOpt = donorTransferRepository
                .findByDonorIdAndRequestedByIdAndRequestedToIdAndRequestType(
                        donorId, fromId, toId, "RELEASE");

        if (transferOpt.isEmpty())
            throw new IllegalArgumentException("No matching release request found");

        DonorTransfer transfer = transferOpt.get();
        StoredDonor donor = transfer.getDonor();

        donor.setDonorCultivator(transfer.getRequestedTo()); // requestedTo is new owner
        donorRepository.save(donor);
        donorTransferRepository.delete(transfer);

        return convertToDto(transfer);
    }


    public List<DonorTransferDto> getPendingRequestsByCultivator(Integer cultivatorId) {
        List<DonorTransfer> transfers = donorTransferRepository.findAllPendingByCultivatorId(cultivatorId);
        return transfers.stream().map(this::convertToDto).toList();
    }

    private DonorTransferDto convertToDto(DonorTransfer transfer) {
        DonorTransferDto dto = new DonorTransferDto();
        dto.setId(transfer.getId());
        dto.setDonorId(transfer.getDonor().getId());
        dto.setRequestedById(transfer.getRequestedBy().getId());
        dto.setRequestedToId(transfer.getRequestedTo().getId());
        dto.setRequestType(transfer.getRequestType());
        dto.setDonorName(transfer.getDonor().getName());
        dto.setRequestedByName(transfer.getRequestedBy().getName());
        dto.setRequestedToName(transfer.getRequestedTo().getName());
        return dto;
    }

}

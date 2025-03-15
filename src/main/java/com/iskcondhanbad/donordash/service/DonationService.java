package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.DonationDto;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.model.Donation;
import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.repository.DonationRepository;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.utils.Services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.iskcondhanbad.donordash.dto.DonationResponseDto;
import com.iskcondhanbad.donordash.dto.EditDonationDto;
import com.iskcondhanbad.donordash.dto.ReceiptDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DonationService {

    @Autowired
    DonationRepository donationRepository;
    @Autowired
    DonorRepository donorRepository;

    @Autowired
    DonorCultivatorService donorCultivatorService;

    @PersistenceContext
    private EntityManager entityManager;

    public Donation donate(DonationDto donationDto) throws Exception {
        Donor donor = donorRepository.findById(donationDto.getDonorId())
                .orElseThrow(() -> new Exception("Donor not found"));

        Donation donation = new Donation();
        donation.setAmount(donationDto.getAmount());
        donation.setPurpose(donationDto.getPurpose());
        donation.setPaymentMode(donationDto.getPaymentMode());
        donation.setTransactionId(donationDto.getTransactionId());
        donation.setStatus(donationDto.getStatus());
        donation.setRemark(donationDto.getRemark());
        donation.setDonor(donor);
        donation.setCreatedAt(donationDto.getCreatedAt() != null ? donationDto.getCreatedAt() : new Date());
        donation.setReceiptId(donationDto.getReceiptId());
        donation.setVerifiedAt(donationDto.getVerifiedAt());
        donation.setPaymentDate(donationDto.getPaymentDate() != null ? donationDto.getPaymentDate()
                : (donationDto.getCreatedAt() != null ? donationDto.getCreatedAt() : new Date()));

        return donationRepository.save(donation);
    }

    public Donation changeStatus(Long donationId, String newStatus) throws Exception {
        Donation donation = findDonationById(donationId);

        String currentStatus = donation.getStatus();

        if ("Cancelled".equalsIgnoreCase(currentStatus) || "Failed".equalsIgnoreCase(currentStatus)) {
            throw new Exception("Cannot change status of a Cancelled or Failed donation");
        }

        if ("Verified".equalsIgnoreCase(currentStatus) && !"Cancelled".equalsIgnoreCase(newStatus)) {
            throw new Exception("Verified status can only be changed to Cancelled");
        }

        if ("Verified".equalsIgnoreCase(newStatus) && donation.getTransactionId() == null
                && !"Cash".equalsIgnoreCase(donation.getPaymentMode())) {
            throw new Exception("Cannot verify non-cash donation without a transaction ID");
        }

        donation.setStatus(newStatus);
        if ("Verified".equalsIgnoreCase(newStatus)) {
            donation.setVerifiedAt(new Date());
            donation.setReceiptId(generateReceiptId(donationId));
        }

        return donationRepository.save(donation);
    }

    public Donation editDonation(Long donationId, EditDonationDto editDonationDto) throws Exception {
        Donation donation = findDonationById(donationId);

        if (!donation.getStatus().equalsIgnoreCase("Pending") && !donation.getStatus().equalsIgnoreCase("Verified")) {
            throw new Exception("Cannot edit a donation with status other than Pending or Verified");
        }

        if (editDonationDto.getPurpose() != null)
            donation.setPurpose(editDonationDto.getPurpose());

        if (editDonationDto.getAmount() != null) {
            donation.setAmount(editDonationDto.getAmount());
        }

        if (editDonationDto.getPaymentMode() != null) {
            donation.setPaymentMode(editDonationDto.getPaymentMode());
        }

        if (editDonationDto.getTransactionId() != null) {
            donation.setTransactionId(editDonationDto.getTransactionId());
        }

        if (editDonationDto.getRemark() != null) {
            donation.setRemark(editDonationDto.getRemark());
        }

        if (donation.getPaymentMode().equalsIgnoreCase("Cash")) {
            donation.setTransactionId(null);
        }

        return donationRepository.save(donation);

    }

    public List<DonationResponseDto> getDonationsByFilter(DonationFilterDto filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Donation> query = cb.createQuery(Donation.class);
        Root<Donation> donation = query.from(Donation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getFromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(donation.get("paymentDate"), filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(donation.get("paymentDate"), filter.getToDate()));
        }
        if (filter.getDonorId() != null) {
            predicates.add(cb.equal(donation.get("donor").get("id"), filter.getDonorId()));
        }
        if (filter.getDonorIds() != null && !filter.getDonorIds().isEmpty()) {
            predicates.add(donation.get("donor").get("id").in(filter.getDonorIds()));
        }
        if (filter.getDonorCultivatorId() != null) {
            predicates.add(
                    cb.equal(donation.get("donor").get("donorCultivator").get("id"), filter.getDonorCultivatorId()));
        }
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(donation.get("status"), filter.getStatus()));
        }
        if (filter.getPaymentMode() != null) {
            predicates.add(cb.equal(donation.get("paymentMode"), filter.getPaymentMode()));
        }
        if (filter.getDonationSupervisorId() != null && filter.getDonationSupervisorId() != 0) {
            predicates.add(cb.equal(donation.get("donationSupervisorId"), filter.getDonationSupervisorId()));
        }
        if (filter.getMinAmount() != 0) {
            predicates.add(cb.greaterThanOrEqualTo(donation.get("amount"), filter.getMinAmount()));
        }
        if (filter.getMaxAmount() != 0) {
            predicates.add(cb.lessThanOrEqualTo(donation.get("amount"), filter.getMaxAmount()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(donation.get("paymentDate")));

        List<Donation> donations = entityManager.createQuery(query).getResultList();
        return donations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private DonationResponseDto convertToDto(Donation donation) {
        DonationResponseDto dto = new DonationResponseDto();
        dto.setId(donation.getId());
        dto.setDonorName(donation.getDonor().getName());
        dto.setAmount(donation.getAmount());
        dto.setPurpose(donation.getPurpose());
        dto.setPaymentMode(donation.getPaymentMode());
        dto.setTransactionId(donation.getTransactionId());
        dto.setStatus(donation.getStatus());
        dto.setRemark(donation.getRemark());
        dto.setDonorId(donation.getDonor().getId());
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setReceiptId(donation.getReceiptId());
        dto.setVerifiedAt(donation.getVerifiedAt());
        dto.setPaymentDate(donation.getPaymentDate());
        dto.setDonorCultivatorName(donation.getDonor().getDonorCultivator().getName());
        return dto;
    }

    private Donation findDonationById(Long donationId) throws Exception {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new Exception("Donation not found"));
    }

    public List<Donation> getDonationsByDonorId(Integer donorId) {
        return donationRepository.findByDonorId(donorId);
    }

    public Map<String, Double> getDonationSumBy(String parameter, Integer cultivatorId, Date dateFrom, Date dateTo) {
        List<Object[]> results;
        switch (parameter.toLowerCase()) {
            case "purpose":
                results = donationRepository.findDonationSumByPurpose(cultivatorId, dateFrom, dateTo);
                break;
            case "zone":
                results = donationRepository.findDonationSumByZone(cultivatorId, dateFrom, dateTo);
                break;
            default:
                throw new IllegalArgumentException("Invalid parameter");
        }

        Map<String, Double> donationSummary = new HashMap<>();
        for (Object[] result : results) {
            donationSummary.put((String) result[0], (Double) result[1]);
        }
        return donationSummary;
    }

    public List<Donation> getAllDonations(Integer cultivatorId, Date dateFrom, Date dateTo) {
        return donationRepository.findAllDonations(cultivatorId, dateFrom, dateTo);
    }

    public String generateReceiptId(Long donationId) throws Exception {
        String receiptId;
        try {
            DonorCultivator donorCultivator = getDonorCultivatorByDonationId(donationId);
            String cultivatorCode = donorCultivator.getShortName().toUpperCase();
            Integer donationsVerified = donorCultivator.getDonationsVerified();
            String formattedDonationsVerified = String.format("%06d", donationsVerified + 1);
            receiptId = String.format("ISKDHN-%s-%s", cultivatorCode, formattedDonationsVerified);
            donorCultivatorService.changeDonationsVerified(donorCultivator.getId(), donationsVerified + 1);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new Exception("Error generating receipt number");
        }
        return receiptId;
    }

    public DonorCultivator getDonorCultivatorByDonationId(Long donationId) throws Exception {
        try {
            Donation donation = findDonationById(donationId);
            return donation.getDonor().getDonorCultivator();
        } catch (Exception e) {
            System.err.println("Error retrieving donor cultivator by donation ID: " + e.getMessage());
            throw new Exception("Error retrieving donor cultivator by donation ID", e);
        }
    }

    public ReceiptDto getReceipt(Long donationId) {
        Donation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null || !donation.getStatus().equals("Verified")) {
            return null;
        }
        Donor donor = donation.getDonor();
        ReceiptDto receipt = new ReceiptDto();

        receipt.setPaymentDate(donation.getPaymentDate().toString());
        receipt.setAmount(donation.getAmount());
        receipt.setDonorName(donor.getName());
        receipt.setDonorAddress(donor.getAddress());
        receipt.setDonorPIN(donor.getPincode());
        receipt.setPan(donor.getPanNumber());
        receipt.setMobile(donor.getMobileNumber());
        receipt.setEmail(donor.getEmail());
        receipt.setVerifiedDate(donation.getVerifiedAt() != null ? donation.getVerifiedAt().toString() : null);
        receipt.setPaymentMode(donation.getPaymentMode());
        receipt.setTransactionID(donation.getTransactionId());
        receipt.setReceiptNumber(donation.getReceiptId());

        return receipt;
    }

}
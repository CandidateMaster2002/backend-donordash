package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.controller.AddDonationResponseDto;
import com.iskcondhanbad.donordash.dto.BulkEditResponseDto;
import com.iskcondhanbad.donordash.dto.DonationDetailsDTO;
import com.iskcondhanbad.donordash.dto.DonationDto;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.model.Donation;
import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.repository.DonationRepository;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;
import com.iskcondhanbad.donordash.utils.Services;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.iskcondhanbad.donordash.dto.DonationResponseDto;
import com.iskcondhanbad.donordash.dto.DonationUpdateErrorDto;
import com.iskcondhanbad.donordash.dto.EditDonationDto;
import com.iskcondhanbad.donordash.dto.EditDonationDtoWithId;
import com.iskcondhanbad.donordash.dto.ReceiptDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DonorCultivator getDonorCultivatorByDonor(Donor donor) {
        if (donor == null) {
            return null;
        }
        return donor.getDonorCultivator();
    }

    public DonorCultivator getDonorCultivatorById(Integer donorCultivatorId) {
        return donorCultivatorRepository.findById(donorCultivatorId).orElse(null);
    }

    public AddDonationResponseDto donate(DonationDto donationDto) throws Exception {
        Donor donor = donorRepository.findById(donationDto.getDonorId())
                .orElseThrow(() -> new Exception("Donor not found"));

        if (!"Cancelled".equalsIgnoreCase(donationDto.getStatus()) && donationDto.getTransactionId() != null) {
            Optional<Donation> existingDonation = donationRepository
                    .findByTransactionId(donationDto.getTransactionId());
            if (existingDonation.isPresent()) {
                return new AddDonationResponseDto(true, existingDonation.get());
            }
        }



        Donation donation = new Donation();
        donation.setAmount(donationDto.getAmount());
        donation.setPurpose(donationDto.getPurpose());
        donation.setPaymentMode(donationDto.getPaymentMode());
        donation.setTransactionId(donationDto.getTransactionId());
        if (donationDto.getCollectedById() != null) {
            DonorCultivator collectedBy = getDonorCultivatorById(donationDto.getCollectedById());
            donation.setCollectedBy(collectedBy);
        } else {
            donation.setCollectedBy(getDonorCultivatorByDonor(donor));
        }
        if (!donation.getCollectedBy().getId().equals(getDonorCultivatorByDonor(donor).getId())) {
            donation.setStatus("Unapproved");
        } else {
            donation.setStatus(donationDto.getStatus());
        }
        donation.setRemark(donationDto.getRemark());
        donation.setDonor(donor);
        donation.setCreatedAt(donationDto.getCreatedAt() != null ? donationDto.getCreatedAt() : new Date());
        donation.setReceiptId(donationDto.getReceiptId());
        donation.setVerifiedAt(donationDto.getVerifiedAt());

        donation.setPaymentDate(donationDto.getPaymentDate() != null
                ? donationDto.getPaymentDate()
                : (donationDto.getCreatedAt() != null ? donationDto.getCreatedAt() : new Date()));

        Donation savedDonation = donationRepository.save(donation);
        return new AddDonationResponseDto(false, savedDonation);
    }

    public BulkEditResponseDto bulkEditDonations(List<EditDonationDtoWithId> donationUpdates) {
        List<Donation> successfulUpdates = new ArrayList<>();
        List<DonationUpdateErrorDto> errors = new ArrayList<>();

        for (EditDonationDtoWithId dto : donationUpdates) {
            try {
                // First update editable fields
                EditDonationDto editDto = new EditDonationDto();
                editDto.setAmount(dto.getAmount());
                editDto.setPurpose(dto.getPurpose());
                editDto.setPaymentMode(dto.getPaymentMode());
                editDto.setTransactionId(dto.getTransactionId());
                editDto.setRemark(dto.getRemark());

                Donation updatedDonation = editDonation(dto.getDonationId(), editDto);

                // If status is provided, try changing it
                if (dto.getStatus() != null) {
                    updatedDonation = changeStatus(dto.getDonationId(), dto.getStatus());
                }

                successfulUpdates.add(updatedDonation);
            } catch (Exception e) {
                errors.add(new DonationUpdateErrorDto(dto.getDonationId(), e.getMessage()));
            }
        }

        return new BulkEditResponseDto(successfulUpdates, errors);
    }

    @Transactional(readOnly = true)
    public List<DonationDetailsDTO> getFilteredDonations(Date startDate, Date endDate, List<String> paymentModes,
            List<String> donationStatuses, List<String> cultivatorNames) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Donation> query = cb.createQuery(Donation.class);
        Root<Donation> donationRoot = query.from(Donation.class);

        Join<Donation, Donor> donorJoin = donationRoot.join("donor", JoinType.INNER);
        Join<Donor, DonorCultivator> cultivatorJoin = donorJoin.join("donorCultivator", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(donationRoot.get("paymentDate"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(donationRoot.get("paymentDate"), endDate));
        }
        if (donationStatuses != null && !donationStatuses.isEmpty()) {
            predicates.add(donationRoot.get("status").in(donationStatuses));
        }

        if (paymentModes != null && !paymentModes.isEmpty()) {
            predicates.add(donationRoot.get("paymentMode").in(paymentModes));
        }
        if (cultivatorNames != null && !cultivatorNames.isEmpty()) {
            predicates.add(cultivatorJoin.get("name").in(cultivatorNames));
        }

        query.select(donationRoot)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(donationRoot.get("paymentDate")));

        List<Donation> donations = entityManager.createQuery(query).getResultList();

        return donations.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private DonationDetailsDTO mapToDTO(Donation donation) {
        Donor donor = donation.getDonor();
        DonorCultivator cultivator = donor.getDonorCultivator();
        DonationDetailsDTO dto = new DonationDetailsDTO();
        dto.setDonationAmount(donation.getAmount());
        dto.setDonationPurpose(donation.getPurpose());
        dto.setPaymentMethod(donation.getPaymentMode());
        dto.setTransactionId(donation.getTransactionId());
        dto.setDonationCollectedBy(donation.getCollectedBy() != null ? donation.getCollectedBy().getName() : "N/A");
        dto.setRemark(donation.getRemark());
        dto.setPaymentDate(donation.getPaymentDate());
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setVerifiedAt(donation.getVerifiedAt());
        dto.setReceiptNumber(donation.getReceiptId());
        dto.setStatus(donation.getStatus());
        dto.setDonationId(donation.getId());
        dto.setDonorName(donor.getName());
        dto.setEmail(donor.getEmail());
        dto.setMobile(donor.getMobileNumber());
        dto.setPanNumber(donor.getPanNumber());
        dto.setFullAddress(donor.getAddress());
        dto.setPincode(donor.getPincode());
        dto.setZone(donor.getZone());
        dto.setConnectedTo(cultivator.getName());

        return dto;
    }

    public Donation changeStatus(Long donationId, String newStatus) throws Exception {
        Donation donation = findDonationById(donationId);

        String currentStatus = donation.getStatus();

        if ("Unapproved".equalsIgnoreCase(currentStatus)) {
            if ("Razorpay".equalsIgnoreCase(donation.getPaymentMode())) {
                newStatus = "Verified";
            } else {
                newStatus = "Pending";
            }
            donation.setStatus(newStatus);
            return donationRepository.save(donation);
        }

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
            // Only generate receipt id if donor's category is not "no_receipt"
            if (donation.getDonor() != null && donation.getDonor().getCategory() != null
                    && !"no_receipt".equalsIgnoreCase(donation.getDonor().getCategory())
                    && !donation.getNotGenerateReceipt()) {
                donation.setReceiptId(generateReceiptId(donationId));
            }
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
        if (filter.getCollectedById() != null) {
            predicates.add(
                    cb.equal(donation.get("collectedBy").get("id"), filter.getCollectedById()));
        }
        if (filter.getDonorCultivatorId() != null && filter.getDonorCultivatorId() != 0) {
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
        dto.setCollectedByName(
                donation.getCollectedBy() != null ? donation.getCollectedBy().getName() : null);
        dto.setDonorCultivatorName(
                donation.getDonor().getDonorCultivator() != null ? donation.getDonor().getDonorCultivator().getName()
                        : null);
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
            case "payment_mode":
                results = donationRepository.findDonationSumByPaymentMode(cultivatorId, dateFrom, dateTo);
                break;
            case "cultivator":
                results = donationRepository.findDonationSumByCultivator(dateFrom, dateTo);
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
            return donation.getCollectedBy() != null ? donation.getCollectedBy()
                    : getDonorCultivatorByDonor(donation.getDonor());
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
        receipt.setDonationId(donation.getId().toString());
        receipt.setDonorAddress(donor.getAddress());
        receipt.setDonorPIN(donor.getPincode());
        receipt.setPurpose(donation.getPurpose());
        receipt.setDonorCultivatorId(
                donor.getDonorCultivator() != null ? donor.getDonorCultivator().getId().toString() : null);
        receipt.setDonorCultivatorName(
                donor.getDonorCultivator() != null ? donor.getDonorCultivator().getName() : null);
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
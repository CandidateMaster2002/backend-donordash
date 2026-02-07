package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.controller.AddDonationResponseDto;
import com.iskcondhanbad.donordash.dto.BulkEditResponseDto;
import com.iskcondhanbad.donordash.dto.DonationDetailsDTO;
import com.iskcondhanbad.donordash.dto.CreateDonationRequest;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.model.StoredDonation;
import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.model.StoredDonorCultivator;
import com.iskcondhanbad.donordash.repository.DonationRepository;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;
import com.iskcondhanbad.donordash.utils.Constants;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.iskcondhanbad.donordash.dto.Donation;
import com.iskcondhanbad.donordash.dto.DonationUpdateErrorDto;
import com.iskcondhanbad.donordash.dto.EditDonationDto;
import com.iskcondhanbad.donordash.dto.EditDonationDtoWithId;
import com.iskcondhanbad.donordash.dto.ReceiptDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AddDonationResponseDto donate(CreateDonationRequest createDonationRequest) {
        // validate donor
        StoredDonor donor = donorRepository.findById(createDonationRequest.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        final String paymentMode = createDonationRequest.getPaymentMode();
        final String transactionId = createDonationRequest.getTransactionId();
        if (!"Cash".equalsIgnoreCase(paymentMode) && (Objects.isNull(transactionId) || transactionId.isBlank())) {
            throw new IllegalArgumentException("transactionId is required");
        }

        // check duplicate by transactionId (only when not cancelled)
        if (Objects.nonNull(transactionId)&&!"Cancelled".equalsIgnoreCase(createDonationRequest.getStatus())) {
            Optional<StoredDonation> existing = donationRepository.findByTransactionId(transactionId);
            if (existing.isPresent()) {
                return new AddDonationResponseDto(true, existing.get());
            }
        }

        StoredDonorCultivator donorCultivator = getDonorCultivatorByDonor(donor);
        StoredDonorCultivator collectedBy = (createDonationRequest.getCollectedById() != null)
                ? getDonorCultivatorById(createDonationRequest.getCollectedById())
                : donorCultivator;

        Date createdAt = (createDonationRequest.getCreatedAt() != null) ? createDonationRequest.getCreatedAt() : new Date();
        Date paymentDate = (createDonationRequest.getPaymentDate() != null) ? createDonationRequest.getPaymentDate() : createdAt;

        StoredDonation donation = new StoredDonation();
        donation.setAmount(createDonationRequest.getAmount());
        donation.setPurpose(createDonationRequest.getPurpose());
        donation.setPaymentMode(createDonationRequest.getPaymentMode());
        donation.setTransactionId(transactionId);

        donation.setCollectedBy(collectedBy);
        if (!Objects.equals(collectedBy.getId(), donorCultivator.getId())) {
            donation.setStatus("Unapproved");
        } else {
            donation.setStatus(createDonationRequest.getStatus());
        }

        donation.setRemark(createDonationRequest.getRemark());
        donation.setDonor(donor);
        donation.setCreatedAt(createdAt);
        donation.setReceiptId(createDonationRequest.getReceiptId());
        donation.setVerifiedAt(createDonationRequest.getVerifiedAt());
        donation.setNotGenerateReceipt(Boolean.TRUE.equals(createDonationRequest.getNotGenerateReceipt()));
        donation.setPaymentDate(paymentDate);

        StoredDonation saved = donationRepository.save(donation);
        return new AddDonationResponseDto(false, saved);
    }



    @Transactional
    public BulkEditResponseDto bulkEditDonations(List<EditDonationDtoWithId> donationUpdates) {
        List<StoredDonation> successfulUpdates = new ArrayList<>();
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
                editDto.setCostCenter(dto.getCostCenter());
                editDto.setPaymentDate(dto.getPaymentDate());

                StoredDonation updatedDonation = editDonation(dto.getDonationId(), editDto);

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
        CriteriaQuery<StoredDonation> query = cb.createQuery(StoredDonation.class);
        Root<StoredDonation> donationRoot = query.from(StoredDonation.class);

        Join<StoredDonation, StoredDonor> donorJoin = donationRoot.join("donor", JoinType.INNER);
        Join<StoredDonor, StoredDonorCultivator> cultivatorJoin = donorJoin.join("donorCultivator", JoinType.INNER);

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

        List<StoredDonation> donations = entityManager.createQuery(query).getResultList();

        return donations.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private DonationDetailsDTO mapToDTO(StoredDonation donation) {
        StoredDonor donor = donation.getDonor();
        StoredDonorCultivator cultivator = donor.getDonorCultivator();
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

    @Transactional
    public StoredDonation changeStatus(Long donationId, String newStatus) throws Exception {
        StoredDonation donation = findDonationById(donationId);

        String currentStatus = donation.getStatus();

//        if ("Unapproved".equalsIgnoreCase(currentStatus)&&"Razorpay".equalsIgnoreCase(donation.getPaymentMode())) {
//            donation.setStatus("Verified");
//            return donationRepository.save(donation);
//        }

        // if ("Cancelled".equalsIgnoreCase(currentStatus) || "Failed".equalsIgnoreCase(currentStatus)) {
        //     throw new Exception("Cannot change status of a Cancelled or Failed donation");
        // }

        // if ("Verified".equalsIgnoreCase(currentStatus) && !"Cancelled".equalsIgnoreCase(newStatus)) {
        //     throw new Exception("Verified status can only be changed to Cancelled");
        // }

        if ("Verified".equalsIgnoreCase(newStatus) && donation.getTransactionId() == null
                && !"Cash".equalsIgnoreCase(donation.getPaymentMode())) {
            throw new Exception("Cannot verify non-cash donation without a transaction ID");
        }

        donation.setStatus(newStatus);
        if(newStatus.equalsIgnoreCase("Cancelled")) {
            String txnId = donation.getTransactionId();
            if (txnId == null || txnId.trim().isEmpty()) {
                txnId = "TXN-" + donation.getId().toString();
            }
            donation.setTransactionId(txnId + "-CANCELLED");
        }
        if ("Verified".equalsIgnoreCase(newStatus)) {
            donation.setVerifiedAt(new Date());
            // Only generate receipt id if donor's category is not "no_receipt"
            if (donation.getDonor() != null && donation.getDonor().getCategory() != null
                    && !"no_receipt".equalsIgnoreCase(donation.getDonor().getCategory())
                    && donation.getNotGenerateReceipt() != Boolean.TRUE) {
                donation.setReceiptId(generateReceiptId(donationId));
            }
        }

        return donationRepository.save(donation);
    }

    @Transactional
    public StoredDonation editDonation(Long donationId, EditDonationDto editDonationDto) throws Exception {
        StoredDonation donation = findDonationById(donationId);

        // if (donation.getStatus().equalsIgnoreCase("Cancelled") ||!donation.getStatus().equalsIgnoreCase("")) {
        //     throw new Exception("Cannot edit a donation with status other than Pending or Verified");
        // }

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

        if (editDonationDto.getCostCenter() != null) {
            donation.setCostCenter(editDonationDto.getCostCenter());
        }

        if(Objects.nonNull(editDonationDto.getPaymentDate())){
            donation.setPaymentDate(editDonationDto.getPaymentDate());
        }

        return donationRepository.save(donation);

    }

    @Transactional(readOnly = true)
    public List<Donation> getDonationsByFilter(DonationFilterDto filter) {
        Date fromDate = validOrDefault(filter.getFromDate(), Constants.DEFAULT_FROM_DATE);
        Date toDate   = validOrDefault(filter.getToDate(), new Date());
        if(fromDate.after(toDate)){
            Date temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        return donationRepository.findDonationsByFilter(
                fromDate,
                toDate,
                filter.getDonorCultivatorId(),
                filter.getDonationSupervisorId(),
                isBlank(filter.getStatus()) ? null : filter.getStatus(),
                filter.getCollectedById(),
                isBlank(filter.getPaymentMode()) ? null : filter.getPaymentMode(),
                filter.getDonorId(),
                (filter.getDonorIds() == null || filter.getDonorIds().isEmpty())
                        ? null
                        : filter.getDonorIds(),
                filter.getMinAmount() > 0 ? filter.getMinAmount() : null,
                filter.getMaxAmount() > 0 ? filter.getMaxAmount() : null
        );
    }



    @Transactional
    private StoredDonation findDonationById(Long donationId) throws Exception {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new Exception("donation not found"));
    }

    @Transactional
    public List<StoredDonation> getDonationsByDonorId(Integer donorId) {
        return donationRepository.findByDonorId(donorId);
    }

    @Transactional
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

    @Transactional
    public List<StoredDonation> getAllDonationsByCultivatorInGivenRange(Integer cultivatorId, Date dateFrom, Date dateTo) {
        return donationRepository.findAllDonationsByCultivatorInGivenRange(cultivatorId, dateFrom, dateTo);
    }

    public String generateReceiptId(Long donationId) throws Exception {
        String receiptId;
        try {
            StoredDonorCultivator donorCultivator = getDonorCultivatorByDonationId(donationId);
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

    public StoredDonorCultivator getDonorCultivatorByDonationId(Long donationId) throws Exception {
        try {
            StoredDonation donation = findDonationById(donationId);
            return donation.getCollectedBy() != null ? donation.getCollectedBy()
                    : getDonorCultivatorByDonor(donation.getDonor());
        } catch (Exception e) {
            System.err.println("Error retrieving donor cultivator by donation ID: " + e.getMessage());
            throw new Exception("Error retrieving donor cultivator by donation ID", e);
        }
    }

    @Transactional
    public ReceiptDto getReceipt(Long donationId) {
        StoredDonation
                donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null || !donation.getStatus().equals("Verified")) {
            return null;
        }
        StoredDonor donor = donation.getDonor();
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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Date validOrDefault(Date date, Date defaultDate) {
        return date != null ? date : defaultDate;
    }

    private StoredDonorCultivator getDonorCultivatorByDonor(StoredDonor donor) {
        if (donor == null) {
            return null;
        }
        return donor.getDonorCultivator();
    }

    private StoredDonorCultivator getDonorCultivatorById(Integer donorCultivatorId) {
        return donorCultivatorRepository.findById(donorCultivatorId).orElse(null);
    }
}
package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.controller.AddDonationResponseDto;
import com.iskcondhanbad.donordash.dto.*;
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
        if (!"Cash".equalsIgnoreCase(paymentMode) && isBlank(transactionId)) {
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
        StoredDonorCultivator collectedBy = (Objects.nonNull(createDonationRequest.getCollectedById()))
                ? getDonorCultivatorById(createDonationRequest.getCollectedById())
                : donorCultivator;

        Date createdAt = (Objects.nonNull(createDonationRequest.getCreatedAt())) ? createDonationRequest.getCreatedAt() : new Date();
        Date paymentDate = (Objects.nonNull(createDonationRequest.getPaymentDate())) ? createDonationRequest.getPaymentDate() : createdAt;

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

    @Transactional(readOnly = true)
    public List<DonationDetailsDTO> getFilteredDonations(Date startDate, Date endDate, List<String> paymentModes,
            List<String> donationStatuses, List<String> cultivatorNames) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StoredDonation> query = cb.createQuery(StoredDonation.class);
        Root<StoredDonation> donationRoot = query.from(StoredDonation.class);

        Join<StoredDonation, StoredDonor> donorJoin = donationRoot.join("donor", JoinType.INNER);
        Join<StoredDonor, StoredDonorCultivator> cultivatorJoin = donorJoin.join("donorCultivator", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if (Objects.nonNull(startDate)) {
            predicates.add(cb.greaterThanOrEqualTo(donationRoot.get("paymentDate"), startDate));
        }
        if (Objects.nonNull(endDate)) {
            predicates.add(cb.lessThanOrEqualTo(donationRoot.get("paymentDate"), endDate));
        }
        if (Objects.nonNull(donationStatuses) && !donationStatuses.isEmpty()) {
            predicates.add(donationRoot.get("status").in(donationStatuses));
        }

        if (Objects.nonNull(paymentModes) && !paymentModes.isEmpty()) {
            predicates.add(donationRoot.get("paymentMode").in(paymentModes));
        }
        if (Objects.nonNull(cultivatorNames) && !cultivatorNames.isEmpty()) {
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
        dto.setDonationCollectedBy(Objects.nonNull(donation.getCollectedBy()) ? donation.getCollectedBy().getName() : "N/A");
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
        if(currentStatus.equalsIgnoreCase(newStatus)) {
            return donation;
        }
        validateStatusChange(currentStatus, newStatus);

        if ("Verified".equalsIgnoreCase(newStatus) && Objects.isNull(donation.getTransactionId())
                && !"Cash".equalsIgnoreCase(donation.getPaymentMode())) {
            throw new Exception("Cannot verify non-cash donation without a transaction ID");
        }

        donation.setStatus(newStatus);
        if(newStatus.equalsIgnoreCase("Cancelled")) {
            String txnId = donation.getTransactionId();
            if (isBlank(txnId)) {
                txnId = "TXN-" + donation.getId().toString();
            }
            donation.setTransactionId(txnId + "-CANCELLED");
        }
        if ("Verified".equalsIgnoreCase(newStatus)) {
            donation.setVerifiedAt(new Date());
            // Only generate receipt id if donor's category is not "no_receipt"
            if (Objects.nonNull(donation.getDonor()) && Objects.nonNull(donation.getDonor().getCategory())
                    && !"no_receipt".equalsIgnoreCase(donation.getDonor().getCategory())
                    && donation.getNotGenerateReceipt() != Boolean.TRUE) {
                donation.setReceiptId(generateReceiptId(donationId));
            }
        }

        return donationRepository.save(donation);
    }

    @Transactional
    public BulkEditResponseDto bulkEditDonations(List<UpdateDonationRequest> donationUpdates) {

        List<StoredDonation> successfulUpdates = new ArrayList<>();
        List<DonationUpdateErrorDto> errors = new ArrayList<>();

        for (UpdateDonationRequest request : donationUpdates) {
            try {

                StoredDonation updatedDonation = updateDonation(request);

                if (Objects.nonNull(request.getStatus())) {
                    updatedDonation = changeStatus(request.getDonationId(), request.getStatus());
                }

                successfulUpdates.add(updatedDonation);

            } catch (Exception ex) {
                errors.add(new DonationUpdateErrorDto(request.getDonationId(), ex.getMessage()));
            }
        }

        return new BulkEditResponseDto(successfulUpdates, errors);
    }

    @Transactional
    public StoredDonation updateDonation(UpdateDonationRequest request) throws Exception {

        StoredDonation donation = findDonationById(request.getDonationId());

        if (Objects.nonNull(request.getPurpose())) {
            donation.setPurpose(request.getPurpose());
        }

        if (Objects.nonNull(request.getAmount())) {
            donation.setAmount(request.getAmount());
        }

        if (Objects.nonNull(request.getPaymentMode())) {
            donation.setPaymentMode(request.getPaymentMode());
        }

        if (Objects.nonNull(request.getTransactionId())) {
            donation.setTransactionId(request.getTransactionId());
        }

        if (Objects.nonNull(request.getRemark())) {
            donation.setRemark(request.getRemark());
        }

        if (Objects.nonNull(request.getCostCenter())) {
            donation.setCostCenter(request.getCostCenter());
        }

        if (Objects.nonNull(request.getPaymentDate())) {
            donation.setPaymentDate(request.getPaymentDate());
        }

        if (Objects.nonNull(donation.getPaymentMode()) &&
                donation.getPaymentMode().equalsIgnoreCase("Cash")) {
            donation.setTransactionId(null);
        }
        return donation;
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
                (Objects.isNull(filter.getDonorIds()) || filter.getDonorIds().isEmpty())
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

    @Transactional(readOnly = true)
    public Map<String, Double> getDonationSummaryBy(SummaryRequest summaryRequest) {
        if (Objects.isNull(summaryRequest)) {
            throw new IllegalArgumentException("summaryRequest must be provided");
        }

        String parameter = summaryRequest.getParameter();
        Integer collectedById = summaryRequest.getCollectedById();
        Date dateFrom = summaryRequest.getDateFrom();
        Date dateTo = summaryRequest.getDateTo();
        String status = summaryRequest.getStatus();

        if (isBlank(parameter)) {
            throw new IllegalArgumentException("parameter must be provided and non-empty");
        }

        final String param = parameter.trim().toLowerCase();
        List<Object[]> results;

        switch (param) {
            case Constants.PURPOSE:
                results = donationRepository.findDonationSumByPurpose(collectedById, dateFrom, dateTo, status);
                break;

            case Constants.ZONE:
                results = donationRepository.findDonationSumByZone(collectedById, dateFrom, dateTo, status);
                break;

            case Constants.PAYMENT_MODE:
                results = donationRepository.findDonationSumByPaymentMode(collectedById, dateFrom, dateTo, status);
                break;

            case Constants.CULTIVATOR:
                results = donationRepository.findDonationSumByCultivator(dateFrom, dateTo, status);
                break;

            default:
                throw new IllegalArgumentException("Invalid parameter: " + parameter);
        }

        if (Objects.isNull(results) || results.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> donationSummary = new LinkedHashMap<>(results.size());
        for (Object[] row : results) {
            String key = (Objects.isNull(row[0])) ? "UNKNOWN" : row[0].toString();
            Double value = convertToDouble(row.length > 1 ? row[1] : null);
            donationSummary.put(key, value);
        }
        return donationSummary;
    }
    
    private static Double convertToDouble(Object number) {
        if (Objects.isNull(number)) return 0.0;
        if (number instanceof Number) {
            return ((Number) number).doubleValue();
        }
        try {
            return Double.parseDouble(number.toString());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
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
            return Objects.nonNull(donation.getCollectedBy()) ? donation.getCollectedBy()
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
        if (Objects.isNull(donation) || !donation.getStatus().equals("Verified")) {
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
                Objects.nonNull(donor.getDonorCultivator()) ? donor.getDonorCultivator().getId().toString() : null);
        receipt.setDonorCultivatorName(
                Objects.nonNull(donor.getDonorCultivator()) ? donor.getDonorCultivator().getName() : null);
        receipt.setPan(donor.getPanNumber());
        receipt.setMobile(donor.getMobileNumber());
        receipt.setEmail(donor.getEmail());
        receipt.setVerifiedDate(Objects.nonNull(donation.getVerifiedAt()) ? donation.getVerifiedAt().toString() : null);
        receipt.setPaymentMode(donation.getPaymentMode());
        receipt.setTransactionID(donation.getTransactionId());
        receipt.setReceiptNumber(donation.getReceiptId());
        receipt.setState(donor.getState());
        receipt.setCity(donor.getCity());
        return receipt;
    }

    private boolean isBlank(String s) {
        return Objects.isNull(s) || s.isBlank();
    }

    private Date validOrDefault(Date date, Date defaultDate) {
        return Objects.nonNull(date) ? date : defaultDate;
    }

    private void validateStatusChange(String currentStatus, String newStatus) throws Exception {
        if (Objects.isNull(currentStatus) || Objects.isNull(newStatus)) {
            throw new Exception("Status cannot be null");
        }

        String current = currentStatus.toLowerCase();
        String next = newStatus.toLowerCase();

        boolean isValid = (current.equals("unapproved") && next.equals("pending")) ||
                (current.equals("pending") && next.equals("cancelled")) ||
                (current.equals("pending") && next.equals("verified")) ||
                (current.equals("verified") && next.equals("cancelled"));//will be removed in future as we should not allow cancelling verified donations, but added for backward compatibility with existing data

        if (!isValid) {
            throw new Exception("Invalid status change from " + currentStatus + " to " + newStatus);
        }
    }

    private StoredDonorCultivator getDonorCultivatorByDonor(StoredDonor donor) {
        if (Objects.isNull(donor)) {
            return null;
        }
        return donor.getDonorCultivator();
    }

    private StoredDonorCultivator getDonorCultivatorById(Integer donorCultivatorId) {
        return donorCultivatorRepository.findById(donorCultivatorId).orElse(null);
    }
}

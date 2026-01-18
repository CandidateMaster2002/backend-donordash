package com.iskcondhanbad.donordash.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iskcondhanbad.donordash.dto.NityaSevaDonorStatusDTO;
import com.iskcondhanbad.donordash.dto.NityaSevaMonthStatusDTO;
import com.iskcondhanbad.donordash.dto.NityaSevaStatusDTO;
import com.iskcondhanbad.donordash.dto.UpdateNityaSevaStatusDTO;
import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.model.NityaSevaStatus;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.NityaSevaStatusRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NityaSevaStatusService {

    private static final String NITYA_SEVAK_TYPE = "Nitya Sevak";

    @Autowired
    private NityaSevaStatusRepository nityaSevaStatusRepository;

    @Autowired
    private DonorRepository donorRepository;

    public List<NityaSevaDonorStatusDTO> getNityaSevaStatus(Integer cultivatorId) {
        List<StoredDonor> donors = cultivatorId == null
                ? donorRepository.findByType(NITYA_SEVAK_TYPE)
                : donorRepository.findByTypeAndCultivatorId(NITYA_SEVAK_TYPE, cultivatorId);

        List<String> months = generateMonthsList();

        List<Integer> donorIds = donors.stream().map(StoredDonor::getId).collect(Collectors.toList());
        List<NityaSevaStatus> existingStatuses = nityaSevaStatusRepository
                .findByDonorIdInAndMonthIn(donorIds, months);

        Map<AbstractMap.SimpleEntry<Integer, String>, NityaSevaStatus> statusMap = existingStatuses.stream()
                .collect(Collectors.toMap(
                        s -> new AbstractMap.SimpleEntry<>(s.getDonor().getId(), s.getMonth()),
                        Function.identity()));

        List<NityaSevaDonorStatusDTO> result = new ArrayList<>();
        for (StoredDonor donor : donors) {
            List<NityaSevaMonthStatusDTO> monthStatuses = new ArrayList<>();
            for (String month : months) {
                NityaSevaStatus status = statusMap.get(new AbstractMap.SimpleEntry<>(donor.getId(), month));
                if (status == null) {
                    monthStatuses.add(new NityaSevaMonthStatusDTO(month, false, false));
                } else {
                    monthStatuses.add(new NityaSevaMonthStatusDTO(
                            status.getMonth(),
                            status.isSweetOrBtg(),
                            status.isNityaSeva()));
                }
            }
            result.add(new NityaSevaDonorStatusDTO(donor.getId(), donor.getName(), monthStatuses));
        }

        return result;
    }

    public void updateNityaSevaStatus(UpdateNityaSevaStatusDTO updateNityaSevaStatusDTO) {
        // Find existing record
        Optional<NityaSevaStatus> existingStatus = nityaSevaStatusRepository
                .findByDonorIdAndMonth(updateNityaSevaStatusDTO.getDonorId(), updateNityaSevaStatusDTO.getMonth());

        NityaSevaStatus status;

        if (existingStatus.isPresent()) {
            status = existingStatus.get();
        } else {
            status = new NityaSevaStatus();
            // Assuming you have a method setDonorId or setDonor; adjust as per your model
            StoredDonor donor = donorRepository.findById(updateNityaSevaStatusDTO.getDonorId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "donor not found with id: " + updateNityaSevaStatusDTO.getDonorId()));
            status.setDonor(donor);
            status.setMonth(updateNityaSevaStatusDTO.getMonth());
            status.setNityaSeva(false);
            status.setSweetOrBtg(false);
        }

        if ("nitya_seva".equalsIgnoreCase(updateNityaSevaStatusDTO.getEventType())) {
            status.setNityaSeva(updateNityaSevaStatusDTO.isFinalValue());
        } else if ("sweet_or_btg".equalsIgnoreCase(updateNityaSevaStatusDTO.getEventType())) {
            status.setSweetOrBtg(updateNityaSevaStatusDTO.isFinalValue());
        }

        nityaSevaStatusRepository.save(status);
    }

    private List<String> generateMonthsList() {
        List<String> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        for (int i = -3; i < 12; i++) {
            months.add(current.minusMonths(i).format(formatter));
        }
        Collections.reverse(months); // Optional: to have chronological order
        return months;
    }

}

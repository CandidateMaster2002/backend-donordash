package com.iskcondhanbad.donordash.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iskcondhanbad.donordash.dto.NityaSevaDonorStatusDTO;
import com.iskcondhanbad.donordash.dto.NityaSevaMonthStatusDTO;
import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.NityaSevaStatus;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.NityaSevaStatusRepository;

@Service
public class NityaSevaStatusService {

    private static final String NITYA_SEVAK_TYPE = "Nitya Sevak";

    @Autowired
    private NityaSevaStatusRepository nityaSevaStatusRepository;

    @Autowired
    private DonorRepository donorRepository;

    public List<NityaSevaDonorStatusDTO> getNityaSevaStatus(Integer cultivatorId) {
        List<Donor> donors;

        if (cultivatorId == null) {
            donors = donorRepository.findByType(NITYA_SEVAK_TYPE);
        } else {
            donors = donorRepository.findByTypeAndCultivatorId(NITYA_SEVAK_TYPE, cultivatorId);
        }

        // System.out.println("Donors: " + donors);

        List<NityaSevaDonorStatusDTO> result = new ArrayList<>();


        

           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth now = YearMonth.now();

        // Prepare 15 months: last 12 including current, plus next 3
        List<String> months = new ArrayList<>();
        for (int i = 12; i > 0; i--) {
            months.add(now.minusMonths(i).format(formatter));
        }
        months.add(now.format(formatter));
        for (int i = 1; i <= 3; i++) {
            months.add(now.plusMonths(i).format(formatter));
        }

        for (Donor donor : donors) {
            List<NityaSevaMonthStatusDTO> monthStatuses = new ArrayList<>();
            for (String month : months) {
                Optional<NityaSevaStatus> statusOpt =
                        nityaSevaStatusRepository.findByDonorIdAndMonth(donor.getId(), month);
                NityaSevaStatus status = statusOpt.orElseGet(() -> {
                    NityaSevaStatus newStatus = new NityaSevaStatus();
                    newStatus.setDonor(donor);
                    newStatus.setMonth(month);
                    newStatus.setSweetOrBtg(false);
                    newStatus.setNityaSeva(false);
                    return nityaSevaStatusRepository.save(newStatus);
                });
                monthStatuses.add(new NityaSevaMonthStatusDTO(
                        status.getMonth(),
                        status.isSweetOrBtg(),
                        status.isNityaSeva()
                        
                ));
            }
            result.add(new NityaSevaDonorStatusDTO(donor.getId(), donor.getName(), monthStatuses));
        }
        return result;
    }
}

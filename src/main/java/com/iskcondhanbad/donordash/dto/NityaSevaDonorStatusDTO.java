package com.iskcondhanbad.donordash.dto;

import lombok.Data;
import java.util.List;

@Data
public class NityaSevaDonorStatusDTO {
    public NityaSevaDonorStatusDTO(Integer id, String name, List<NityaSevaMonthStatusDTO> monthStatuses) {
        this.donorId = id;
        this.donorName = name;
        this.months = monthStatuses;
    }
    private Integer donorId;
    private String donorName;
    private List<NityaSevaMonthStatusDTO> months;

    
}
package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorTransferDto {
    private Integer id;
    private Integer donorId;
    private Integer requestedById;
    private Integer requestedToId;
    private String requestType;
    private String donorName;
    private String requestedByName;
    private String requestedToName;
}


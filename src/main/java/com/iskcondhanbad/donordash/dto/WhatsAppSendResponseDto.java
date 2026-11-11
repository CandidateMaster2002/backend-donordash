package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendResponseDto {
    private boolean success;
    private String message;
    private String messageId;
    private String recipientMobile;
    private String mediaId;
    private String receiptId;
    private Double amount;
    private String donorName;
}


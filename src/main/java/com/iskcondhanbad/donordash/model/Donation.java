package com.iskcondhanbad.donordash.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donation")

public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private String paymentMode;

    @Column(nullable = true, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = true)
    private String remark;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Donor donor;


    //this is by default the cultivator of the donor but can be someone else if told
    @ManyToOne
    @JoinColumn(name = "collected_by_id", nullable = true)  
    private DonorCultivator collectedBy;

   @Column(nullable = false)
   private Date paymentDate;
   
    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = true)
    private Boolean notGenerateReceipt;

    @Column(nullable = true, unique = true)
    private String receiptId;

    @Column(nullable = true)
    private Date verifiedAt;

    @Column(nullable = true)
    private String costCenter;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "Pending"; // Ensures default value if not set
        }
        if (this.createdAt == null) {
            this.createdAt = new Date(); // Automatically sets creation date
        }
    }


}
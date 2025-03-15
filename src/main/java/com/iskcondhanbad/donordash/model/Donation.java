package com.iskcondhanbad.donordash.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
    private Donor donor;

   @Column(nullable = false)
   private Date paymentDate;
   
    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = true, unique = true)
    private String receiptId;

    @Column(nullable = true)
    private Date verifiedAt;

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
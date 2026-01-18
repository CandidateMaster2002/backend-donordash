package com.iskcondhanbad.donordash.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donor_transfer")


public class DonorTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private StoredDonor donor;

    // Cultivator who requested the transfer
    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private StoredDonorCultivator requestedBy;

    // Cultivator who is being requested to give/take
    @ManyToOne
    @JoinColumn(name = "requested_to", nullable = false)
    private StoredDonorCultivator requestedTo;

    // ACQUIRE or RELEASE
    @Column(name = "request_type", nullable = false)
    private String requestType;

    
}
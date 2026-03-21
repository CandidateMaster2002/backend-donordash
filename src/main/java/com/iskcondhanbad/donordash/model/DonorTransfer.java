package com.iskcondhanbad.donordash.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donor_transfer")


public class DonorTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private StoredDonor donor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by", nullable = false)
    @ToString.Exclude
    private StoredDonorCultivator requestedBy;

    // Cultivator who is being requested to give/take
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_to", nullable = false)
    @ToString.Exclude
    private StoredDonorCultivator requestedTo;

    // ACQUIRE or RELEASE
    @Column(name = "request_type", nullable = false)
    private String requestType;

    
}
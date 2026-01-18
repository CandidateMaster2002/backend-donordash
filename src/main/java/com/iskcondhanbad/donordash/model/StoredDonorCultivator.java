package com.iskcondhanbad.donordash.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "donor_cultivator")
public class StoredDonorCultivator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shortName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String imageSignature;

    @Column(nullable = true)
    private String remark;

    
    @Column(name="donations_verified", nullable = false)
    private Integer donationsVerified;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_supervisor_id", nullable = false)
    private DonationSupervisor donationSupervisor;

    // Getters and setters
}

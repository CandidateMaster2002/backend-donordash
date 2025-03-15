package com.iskcondhanbad.donordash.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;


@Data
@Entity
public class DonorCultivator {

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

    @ManyToOne
    @JoinColumn(name = "donation_supervisor_id", nullable = false)
    private DonationSupervisor donationSupervisor;

    // Getters and setters
}

package com.iskcondhanbad.donordash.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donor")


public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String type = "One Timer";

    @Column(nullable = true)
    private String photoPath;

    @Column(nullable = false)
    private String category;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String mobileNumber;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String city;

    @Column(nullable = true)
    private String zone; 

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true, unique = true)
    private String panNumber;

    @Column(nullable = true)
    private String remark;

    @ManyToOne
    @JoinColumn(name = "donor_cultivator_id", nullable = false)
    private DonorCultivator donorCultivator;

    @JsonManagedReference
    @OneToMany(mappedBy = "donor")
    private List<Donation> donations;

    @JsonManagedReference
    @OneToMany(mappedBy = "donor")
    private List<SpecialDay> specialDays;

    // Getters and setters
}
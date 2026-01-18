package com.iskcondhanbad.donordash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "donor")
public class StoredDonor {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_cultivator_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private StoredDonorCultivator donorCultivator;

    @JsonManagedReference
    @OneToMany(mappedBy = "donor", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<StoredDonation> donations;

    @JsonManagedReference
    @OneToMany(mappedBy = "donor", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<SpecialDay> specialDays;

    // Getters and setters
}
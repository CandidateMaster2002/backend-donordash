package com.iskcondhanbad.donordash.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.util.*;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Data
@Entity
public class SpecialDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = true)
    private String otherPurpose;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    @JsonBackReference
    private StoredDonor donor;

}

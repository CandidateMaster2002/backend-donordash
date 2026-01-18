
package com.iskcondhanbad.donordash.model;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "nitya_seva_status", uniqueConstraints = {@UniqueConstraint(columnNames = {"donor_id", "month"})})
public class NityaSevaStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private StoredDonor donor;

    @Column(nullable = false)
    private String month; // Format: MM-YYYY

    @Column(name="sweet_or_btg" , nullable = false)
@org.hibernate.annotations.ColumnDefault("false")
private boolean sweetOrBtg = false;

    @Column(nullable = false)
    private boolean nityaSeva;

} 
    




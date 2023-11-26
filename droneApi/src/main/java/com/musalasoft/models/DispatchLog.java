package com.musalasoft.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.musalasoft.enums.Model;
import com.musalasoft.enums.State;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalTime;

@Entity
@Data
public class DispatchLog {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serialNumber;

    @JsonIgnore
    private String dispatchNumber;

    @JsonIgnore
    private LocalTime dispatchTime;

    private LocalTime expectedDeliveryTime;

    private LocalTime expectedReturnTime;

    @Enumerated(EnumType.STRING)
    private Model droneModel;

    @Enumerated(EnumType.STRING)
    private State deliveryStatus;

    private double medicationTotalWeight;

    private Timestamp timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

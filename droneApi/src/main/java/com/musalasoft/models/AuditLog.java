package com.musalasoft.models;

import com.musalasoft.enums.Status;
import lombok.Data;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class AuditLog {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String serialNumber;

    private String batteryCapacity;

    @Enumerated(EnumType.STRING)
    private Status batteryStatus;

    private Timestamp timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

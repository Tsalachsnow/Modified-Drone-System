package com.musalasoft.schedular;

import com.musalasoft.models.AuditLog;
import com.musalasoft.dto.AvailableDronesResponse;
import com.musalasoft.services.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.sql.Timestamp;

@RequiredArgsConstructor
@Slf4j
@Service
public class BatteryCapacitySchedular {

    @Value("${fetch.all.drones.url}")
    private String url;

    private final AuditLogRepository auditLogRepository;
    @Scheduled(fixedDelay = 40000)
    public void status(){

        AvailableDronesResponse drones;
        log.info("40 seconds Scheduled HTTP Response");
        RestTemplate restTemplate = new RestTemplate();
        try {
            drones =  restTemplate.getForObject(url, AvailableDronesResponse.class);
            log.info("response from fetch all drones rest call :: "+ drones);
            drones.getDrones().forEach(x->{
                AuditLog auditLog = new AuditLog();
                auditLog.setSerialNumber(x.getSerialNumber());
                auditLog.setBatteryCapacity(x.getBatteryCapacity());
                auditLog.setBatteryStatus(x.getBatteryStatus());
                auditLog.setTimestamp(new Timestamp(System.currentTimeMillis()));
                auditLogRepository.save(auditLog);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

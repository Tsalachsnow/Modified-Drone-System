package com.musalasoft.schedular;

import com.musalasoft.models.Drone;
import com.musalasoft.enums.Status;
import com.musalasoft.services.repositories.DroneServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChargeBatterySchedular{
    @Autowired
    private DroneServiceRepository droneServiceRepository;

    public void charge(Drone drone) {
        try{

            ScheduledExecutorService scheduledExecutorService =
                    Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
            ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(new Callable() {
                public Object call() throws Exception {
                    if(drone.getBatteryCapacity() < 100 || drone.getBatteryStatus() != Status.CHARGING){
                        drone.setBatteryCapacity(drone.getBatteryCapacity() + 1);
                        droneServiceRepository.save(drone);
                        log.info("Charging drone Battery :: "+ drone.getBatteryCapacity());
                        if(drone.getBatteryCapacity() == 100){
                            log.info("Drone Battery Fully Charged :: "+ drone.getBatteryCapacity());
                            drone.setBatteryStatus(Status.FULLY_CHARGED);
                            droneServiceRepository.save(drone);
                            scheduledExecutorService.shutdown();
                        }
                    }else{
                        log.info("Drone not fully charged :: "+ drone.getBatteryCapacity());
                        scheduledExecutorService.shutdown();
                    }
                    return "charging";
                }
            }, 3, TimeUnit.SECONDS);
            System.out.println(scheduledFuture.get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

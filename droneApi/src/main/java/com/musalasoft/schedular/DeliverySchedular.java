package com.musalasoft.schedular;

import com.musalasoft.models.Drone;
import com.musalasoft.models.Medication;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.services.repositories.DroneServiceRepository;
import com.musalasoft.services.repositories.MedicationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class DeliverySchedular {

    @Autowired
    private DroneServiceRepository droneServiceRepository;

    public void deliver(Drone drone, String expectedArrivalTime) {
        LocalTime time1 = LocalTime.now();
        LocalTime time = LocalTime.of(time1.getHour(), time1.getMinute(), time1.getSecond());
        LocalTime time2 = LocalTime.parse(expectedArrivalTime);
        LocalTime expTime = LocalTime.of(time2.getHour(), time2.getMinute(), time2.getSecond());

        try {
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

            ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(new Callable() {
                public Object call() throws Exception {
                    if (drone.getBatteryCapacity() <= 0) {
                        log.info("Battery Drained:: " + drone.getBatteryCapacity());
                        Thread.currentThread().stop();
                        scheduledExecutorService.shutdown();
                    }
                    if (time.isBefore(expTime)) {
                        drone.setBatteryCapacity(drone.getBatteryCapacity() - 1);
                        drone.setState(State.DELIVERING);
                        drone.setBatteryStatus(Status.DISCHARGING);
                        droneServiceRepository.save(drone);
                        System.out.println(Thread.currentThread().getName());
                        log.info("Delivering with battery capacity  :: " + drone.getBatteryCapacity());
                        log.info("time is equal :: " + time);
                    }
                    if (time.isAfter(expTime)) {
                        drone.setState(State.DELIVERED);
                        drone.setBatteryStatus(Status.NOT_CHARGING);
                        droneServiceRepository.save(drone);
                        log.info("delivered Successfully :: " + drone.getState());
                        Thread.currentThread().stop();
                    }
                    else {
                        log.info("Delivery in progress :: " + drone.getState());
                    }
                    return "Delivering";
                }
            }, 20, TimeUnit.SECONDS);
            System.out.println(scheduledFuture.get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


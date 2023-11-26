package com.musalasoft.schedular;

import com.musalasoft.models.Drone;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.services.repositories.DroneServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class ReturnDroneSchedular {
    @Autowired
    private DroneServiceRepository droneServiceRepository;

    public void returnDrone(Drone drone, LocalTime returnExpectedTime) {
        LocalTime time1 = LocalTime.now();
        LocalTime time = LocalTime.of(time1.getHour(), time1.getMinute(), time1.getSecond());

        try {
            ScheduledExecutorService scheduledExecutorService =
                    Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

            ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(new Callable() {
                public Object call() throws Exception {
                    if (time.isBefore(returnExpectedTime)) {
                        drone.setBatteryCapacity(drone.getBatteryCapacity() - 1);
                        drone.setState(State.RETURNING);
                        drone.setBatteryStatus(Status.DISCHARGING);
                        droneServiceRepository.save(drone);
                        System.out.println(Thread.currentThread().getName());
                        log.info("Drone returning with battery capacity  :: " + drone.getBatteryCapacity());
                        log.info("time is equal :: " + time);
                    }
                    if (time.isAfter(returnExpectedTime)) {
                        drone.setState(State.IDLE);
                        drone.setWeightLimitLeft(drone.getWeightLimit());
                        drone.setBatteryStatus(Status.NOT_CHARGING);
                        droneServiceRepository.save(drone);
                        log.info("Drone "+drone.getSerialNumber()+" returned Successfully :: " + drone.getState());
                        Thread.currentThread().stop();
                    }
                    else {
                        log.info("returning in progress :: " + drone.getState());
                    }
                    return "Returning";
                }
            }, 20, TimeUnit.SECONDS);
            System.out.println(scheduledFuture.get());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

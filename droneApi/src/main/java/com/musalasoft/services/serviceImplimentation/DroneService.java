package com.musalasoft.services.serviceImplimentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musalasoft.models.DispatchLog;
import com.musalasoft.models.Drone;
import com.musalasoft.models.Medication;
import com.musalasoft.schedular.ChargeBatterySchedular;
import com.musalasoft.schedular.DeliverySchedular;
import com.musalasoft.dto.*;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.util.FormatterUtil;
import com.musalasoft.util.ResponseCodes;
import com.musalasoft.util.exception.GenericException;
import com.musalasoft.util.exception.NoDataFoundException;
import com.musalasoft.services.repositories.DispatchLogRepository;
import com.musalasoft.services.repositories.DroneServiceRepository;
import com.musalasoft.services.repositories.MedicationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class DroneService {
    private final DroneServiceRepository droneServiceRepository;
    private final DispatchLogRepository dispatchRepo;
    private final MedicationServiceRepository medication;
    private final ChargeBatterySchedular battery;
    private final DeliverySchedular dispatch;
    private final Environment environment;

    public DroneRegistrationResponse registerDrone(DroneRegistrationRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        DroneRegistrationResponse response = new DroneRegistrationResponse();
        try {
            Drone drone = objectMapper.convertValue(request, Drone.class);
            drone.setWeightLimit(
                    request.getModel().name().equalsIgnoreCase("Lightweight")?
                            Double.parseDouble(Objects.requireNonNull(environment.getProperty("light_weight"))):
                            request.getModel().name().equalsIgnoreCase("Cruiserweight")?
                                    Double.parseDouble(Objects.requireNonNull(environment.getProperty("cruiser_weight"))):
                                    request.getModel().name().equalsIgnoreCase("Middleweight")?
                                            Double.parseDouble(Objects.requireNonNull(environment.getProperty("middle_weight"))):
                                            Double.parseDouble(Objects.requireNonNull(environment.getProperty("heavy_weight"))));
            drone.setState(State.IDLE);
            drone.setBatteryStatus(Status.NOT_CHARGING);
            drone.setWeightLimitLeft(drone.getWeightLimit());
            log.info("Drone registration request received: {}", drone);
            if(droneServiceRepository.findById(drone.getSerialNumber()).isPresent()){
                response.setSerialNumber(drone.getSerialNumber());
                response.setResponseCode(ResponseCodes.ALREADY_REGISTERED);
                response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.ALREADY_REGISTERED));
            }else{
                if(drone.getBatteryCapacity() < 25) {
                    drone.setBatteryStatus(Status.LOW_BATTERY);
                }
                droneServiceRepository.save(drone);
                response.setResponseCode(ResponseCodes.SUCCESS);
                response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.REGISTERED_OK));
                response.setSerialNumber(request.getSerialNumber());
            }
        } catch (Exception e) {
            log.info(Arrays.toString(e.getStackTrace()));
            response.setResponseCode(ResponseCodes.INTERFACE_ERROR);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
            response.setSerialNumber("");
//            throw new GenericException("E91", e.getMessage(), null);

        }
        log.info("Drone registration response: {}", response);
        return response;
    }

    public AvailableDronesResponse droneAvailable(){
        AvailableDronesResponse availableDronesResponse = new AvailableDronesResponse();
        List<DroneDto> drones = new ArrayList<>();
        try {
            List<Drone> drones1 = droneServiceRepository.findAllByState(State.IDLE);

            if(drones1.isEmpty()){
                availableDronesResponse.setResponseCode(ResponseCodes.DRONES_UNAVAILABLE);
                availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DRONES_UNAVAILABLE));
                availableDronesResponse.setDrones(drones);
                return availableDronesResponse;
            }
            availableDronesResponse.setResponseCode(ResponseCodes.SUCCESS);
            availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.AVAIL_DRONES));
            drones1.forEach(drone -> {
                drones.add(new DroneDto().setModel(drone.getModel())
                        .setWeightLimit(FormatterUtil.decimalFormatter(drone.getWeightLimit())+"gr.")
                        .setWeightLimitLeft(FormatterUtil.decimalFormatter(drone.getWeightLimitLeft())+"gr.")
                        .setSerialNumber(drone.getSerialNumber())
                        .setState(drone.getState())
                        .setBatteryCapacity(FormatterUtil.decimalFormatter(drone.getBatteryCapacity())+"%")
                        .setBatteryStatus(drone.getBatteryStatus()));
            });
            availableDronesResponse.setDrones(drones);
            log.info("Available drone Response: {}", availableDronesResponse);

        }catch (Exception e){
            log.info("Error Occurred, Cause:: "+ ExceptionUtils.getStackTrace(e));
            availableDronesResponse.setResponseCode(ResponseCodes.INTERFACE_ERROR);
            availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
        }
        log.info("Available drone Response: {}", availableDronesResponse);
        return availableDronesResponse;
    }

    public BatteryCapacityResponse batteryCapacity(String serialNumber){
        BatteryCapacityResponse batteryCapacityResponse = new BatteryCapacityResponse();
        try {
            Optional<Drone> droneOptional = droneServiceRepository.findById(serialNumber);
            if (droneOptional.isPresent()) {
                Drone drone = droneOptional.get();
                batteryCapacityResponse.setResponseCode(ResponseCodes.SUCCESS);
                batteryCapacityResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.SUCCESS));
                batteryCapacityResponse.setSerialNumber(serialNumber);
                batteryCapacityResponse.setBatteryCapacity(FormatterUtil.decimalFormatter(drone.getBatteryCapacity())+"%");
                batteryCapacityResponse.setBatteryStatus(drone.getBatteryStatus());

            }else {
                batteryCapacityResponse.setResponseCode(ResponseCodes.DRONE_NOT_FOUND);
                batteryCapacityResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DRONE_NOT_FOUND));
            }
               }catch(Exception e){
                log.info(Arrays.toString(e.getStackTrace()));
                batteryCapacityResponse.setResponseCode(ResponseCodes.INTERFACE_ERROR);
                batteryCapacityResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
            }
               log.info("Battery Capacity Response: {}", batteryCapacityResponse);
            return batteryCapacityResponse;
        }

    public AvailableDronesResponse fetchAllDrones(){
        AvailableDronesResponse availableDronesResponse = new AvailableDronesResponse();
        List<DroneDto> drones = new ArrayList<>();
        try {
            List<Drone> drones1 = droneServiceRepository.findAll();
            if(drones1.isEmpty()){
                availableDronesResponse.setResponseCode(ResponseCodes.NO_DRONES_FOUND);
                availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.NO_DRONES_FOUND));
                availableDronesResponse.setDrones(drones);
                return availableDronesResponse;
            }
            availableDronesResponse.setResponseCode(ResponseCodes.SUCCESS);
            availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DRONES_FETCHED));
            drones1.forEach(drone->{
                drones.add(new DroneDto().setModel(drone.getModel())
                        .setWeightLimit(FormatterUtil.decimalFormatter(drone.getWeightLimit())+"gr.")
                        .setWeightLimitLeft(FormatterUtil.decimalFormatter(drone.getWeightLimitLeft())+"gr.")
                        .setSerialNumber(drone.getSerialNumber())
                        .setState(drone.getState())
                        .setBatteryCapacity(FormatterUtil.decimalFormatter(drone.getBatteryCapacity())+"%")
                        .setBatteryStatus(drone.getBatteryStatus()));
            });
            availableDronesResponse.setDrones(drones);
            log.info("Available drone Response: {}", availableDronesResponse);

        }catch (Exception e){
            log.info(Arrays.toString(e.getStackTrace()));
            availableDronesResponse.setResponseCode(ResponseCodes.INTERFACE_ERROR);
            availableDronesResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
        }
        log.info("Available drone Response: {}", availableDronesResponse);
        return availableDronesResponse;

}

    public ChargeBatteryResponse chargeBattery(GenericDroneRequest request){
        log.info("Charging initiated do not unplug the drone");
        ChargeBatteryResponse chargeBatteryResponse = new ChargeBatteryResponse();
        try{
            Drone drone = droneServiceRepository.getById(request.getSerialNumber());
            if(drone.getBatteryStatus().equals(Status.CHARGING)){
                chargeBatteryResponse.setResponseCode(ResponseCodes.CHARGING_BATTERY);
                chargeBatteryResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.CHARGING_BATTERY));
                chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
                chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
                return chargeBatteryResponse;
            }else if(drone.getBatteryStatus().equals(Status.FULLY_CHARGED)){
                chargeBatteryResponse.setResponseCode(ResponseCodes.FULLY_CHARGED);
                chargeBatteryResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.FULLY_CHARGED));
                chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
                chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
                return chargeBatteryResponse;
            }
            drone.setBatteryStatus(Status.CHARGING);
            chargeBatteryResponse.setResponseCode(ResponseCodes.SUCCESS);
            chargeBatteryResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.SUCCESS));
            chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
            chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
            droneServiceRepository.save(drone);
            CompletableFuture.supplyAsync(() ->{
                battery.charge(drone);
                return chargeBatteryResponse;
            }).thenApply(ChargeBatteryResponse -> {
                while(drone.getBatteryCapacity() < 100){
                    battery.charge(drone);
                }
                drone.setBatteryStatus(Status.FULLY_CHARGED);
                droneServiceRepository.save(drone);
                chargeBatteryResponse.setResponseCode(ResponseCodes.SUCCESS);
                chargeBatteryResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.SUCCESS));
                chargeBatteryResponse.setSerialNumber(drone.getSerialNumber());
                chargeBatteryResponse.setStatus(drone.getBatteryStatus().toString());
                log.info("Charge Battery Response: {}", chargeBatteryResponse);
                return chargeBatteryResponse;
            });
            return chargeBatteryResponse;
        }catch (Exception e) {
            log.error("Error occurred while performing charge Battery Request: {}", ExceptionUtils.getStackTrace(e));

            throw new GenericException("E91", e.getMessage(), null);
        }
    }

    public DispatchResponse dispatchDrone(DispatchRequest request){
        String dispatchNo = UUID.randomUUID().toString().substring(0, 8);
        DispatchLog logger = new DispatchLog();
        DispatchResponse dispatchResponse = new DispatchResponse();
        try{
            Optional<DispatchLog> dispatchLog = dispatchRepo.findByDispatchNumber(dispatchNo);
            try{
            if(!dispatchLog.isPresent()){
        List<Medication> med = medication.findAllByDrone_SerialNumberAndStatus(request.getSerialNumber(), State.LOADED);
        Drone drone = droneServiceRepository.getById(request.getSerialNumber());

        LocalTime time1 = LocalTime.now();
        LocalTime time = LocalTime.of(time1.getHour(), time1.getMinute(), time1.getSecond());
        LocalTime expectedTime = LocalTime.parse(request.getEstimatedTimeOfDelivery());
        LocalTime dfgh = LocalTime.of(expectedTime.getHour(), expectedTime.getMinute(), expectedTime.getSecond());

        if (drone.getState().equals(State.DELIVERING)) {
            dispatchResponse.setResponseCode(ResponseCodes.DELIVERING);
            dispatchResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DELIVERING));
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
            return dispatchResponse;
        }else if(drone.getState() != State.LOADED && drone.getState() != State.IDLE){
            dispatchResponse.setResponseCode(ResponseCodes.UNAVAILABLE);
            dispatchResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.UNAVAILABLE));
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }else if (drone.getState().equals(State.IDLE)){
            dispatchResponse.setResponseCode(ResponseCodes.DRONE_IDLE);
            dispatchResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DRONE_IDLE));
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }else if(time.isAfter(dfgh)){
            dispatchResponse.setResponseCode(ResponseCodes.INVALID_TIME);
            dispatchResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INVALID_TIME));
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
            dispatchResponse.setExpectedDeliveryTime(dfgh);
            return dispatchResponse;
        }
            drone.setState(State.DELIVERING);
            droneServiceRepository.save(drone);
                med.forEach(x->{
                    x.setStatus(State.DELIVERING);
                    medication.save(x);
                });
            log.info("Logger for Dispatch:: "+ "Drone with serial number "
                    + request.getSerialNumber() + " dispatched successfully");
            dispatchResponse.setResponseCode(ResponseCodes.SUCCESS);
            dispatchResponse.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.DISPATCH_OK));
            dispatchResponse.setDispatchNumber(dispatchNo);
            dispatchResponse.setCurrentState(drone.getState());
        dispatchResponse.setExpectedDeliveryTime(dfgh);
        CompletableFuture.supplyAsync(() ->{
            dispatch.deliver(drone, request.getEstimatedTimeOfDelivery());
            log.info("current time: {}", dispatchResponse);
            return dispatchResponse;
        }).thenApply(DispatchResponse -> {
            while(time.isBefore(dfgh)){
                dispatch.deliver(drone, request.getEstimatedTimeOfDelivery());
                dispatchResponse.setCurrentState(State.DELIVERED);
            }
            return dispatchResponse;
        });
        med.forEach(x->{
            x.setStatus(State.DELIVERED);
            medication.save(x);
        });
        logger.setDroneModel(drone.getModel());
        logger.setSerialNumber(drone.getSerialNumber());
        logger.setTimestamp(new Timestamp(System.currentTimeMillis()));
        logger.setExpectedDeliveryTime(dfgh);
        logger.setDeliveryStatus(dispatchResponse.getCurrentState());
        logger.setDispatchNumber(dispatchNo);
        double totalWeight = drone.getWeightLimit() - drone.getWeightLimitLeft();
        logger.setMedicationTotalWeight(totalWeight == 0? drone.getWeightLimit():totalWeight);
        logger.setDispatchTime(LocalTime.now());
        dispatchRepo.save(logger);
        dispatchResponse.setDispatchNumber(dispatchNo);
        return dispatchResponse;
            }
         }catch (Exception ignored) {
            } throw new GenericException(ResponseCodes.INVALID_DATA, ResponseCodes.getErrorMessage(ResponseCodes.INVALID_DATA), null);
        }catch (Exception e) {
            log.error("Error occurred while performing Dispatch For Loaded Items: {}", ExceptionUtils.getStackTrace(e));

            throw new GenericException(ResponseCodes.INTERFACE_ERROR, ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR), null);
        }
    }

    public DroneCurrentStatusResponse getDroneCurrentStatus(String serialNumber) {
        DroneCurrentStatusResponse response = new DroneCurrentStatusResponse();
        Optional<Drone> droneOptional = droneServiceRepository.findById(serialNumber);
        if(droneOptional.isPresent()){
            Drone drone = droneOptional.get();
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.STATUS_OK));
            response.setBatteryCapacity(FormatterUtil.decimalFormatter(drone.getBatteryCapacity())+"%");
            response.setDroneCurrentState(drone.getState());
            response.setDroneModel(drone.getModel());
            response.setBatteryStatus(drone.getBatteryStatus());
            return response;
        }else{
            throw new GenericException(ResponseCodes.DRONE_NOT_FOUND, ResponseCodes.getErrorMessage(ResponseCodes.DRONE_NOT_FOUND), null);
        }
    }

}

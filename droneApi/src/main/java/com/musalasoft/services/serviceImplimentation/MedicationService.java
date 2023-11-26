package com.musalasoft.services.serviceImplimentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.musalasoft.models.DispatchLog;
import com.musalasoft.models.Drone;
import com.musalasoft.models.Medication;
import com.musalasoft.dto.*;
import com.musalasoft.enums.State;
import com.musalasoft.enums.Status;
import com.musalasoft.util.FormatterUtil;
import com.musalasoft.util.ResponseCodes;
import com.musalasoft.util.exception.GenericException;
import com.musalasoft.schedular.ReturnDroneSchedular;
import com.musalasoft.services.repositories.DispatchLogRepository;
import com.musalasoft.services.repositories.DroneServiceRepository;
import com.musalasoft.services.repositories.MedicationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.musalasoft.enums.State.LOADED;

@RequiredArgsConstructor
@Slf4j
@Service
public class MedicationService {
    private final DroneServiceRepository droneServiceRepository;
    private final MedicationServiceRepository medicationServiceRepository;
    private final DispatchLogRepository dispatchRepo;
    private final DispatchLogRepository dispatchLogRepository;
    private final ReturnDroneSchedular returnDroneSchedular;

    public LoadMedicationResponse.Response loadMedication(LoadMedicationRequest.Request request) {
        ObjectMapper obj = new ObjectMapper();
        LoadMedicationResponse.Response response = new LoadMedicationResponse.Response();
        try {
            Optional<Drone> droneOptional = droneServiceRepository.findById(request.getSerialNumber());
            if(!droneOptional.isPresent()){
              logAndSetResponse(ResponseCodes.DRONE_NOT_FOUND, ResponseCodes.getErrorMessage(ResponseCodes.DRONE_NOT_FOUND), request.getSerialNumber(), response);
              return response;
            }
            Drone drone = droneOptional.get();
            AtomicReference<Double> total = new AtomicReference<>(0.0);;

            double limit = drone.getWeightLimitLeft();
            log.info("Drone weight limit left: {}", limit);

            request.getMedications().forEach(medication -> total.updateAndGet(v -> v + (medication.getWeight())));

            log.info("Total weight of the medication is: {}", total);

            if (limit - total.get() < 0) {

                logAndSetResponse(ResponseCodes.getErrorMessage(ResponseCodes.WEIGHT_LIMIT_EXCEEDED)+ " "+ FormatterUtil.decimalFormatter(limit)+ "gr. Extra", ResponseCodes.WEIGHT_LIMIT_EXCEEDED, request.getSerialNumber(), response);

            } else if (drone.getBatteryCapacity() < 25) {

                logAndSetResponse(ResponseCodes.getErrorMessage(ResponseCodes.LOW_BATTERY_CAPACITY), ResponseCodes.LOW_BATTERY_CAPACITY, request.getSerialNumber(), response);

            } else if (drone.getBatteryStatus() == Status.CHARGING) {

                logAndSetResponse(ResponseCodes.getErrorMessage(ResponseCodes.CHARGING_BATTERY), ResponseCodes.CHARGING_BATTERY, request.getSerialNumber(), response);

            } else if (drone.getState() == State.LOADING) {

                logAndSetResponse(ResponseCodes.getErrorMessage(ResponseCodes.LOADING), ResponseCodes.LOADING, request.getSerialNumber(), response);

            } else if (Arrays.asList(State.DELIVERING, State.DELIVERED, State.RETURNING).contains(drone.getState())) {

                logAndSetResponse(ResponseCodes.getErrorMessage(ResponseCodes.BUSY), ResponseCodes.BUSY, request.getSerialNumber(), response);

            } else {
                log.info("loading medication");
                drone.setState(State.LOADING);
                droneServiceRepository.save(drone);

                double finalLimit = limit - total.get();
                log.info("finalLimit :: {}", finalLimit);

                request.getMedications().forEach(medication -> {
                    Medication med = obj.convertValue(medication, Medication.class);
                    drone.setState(LOADED);
                    drone.setWeightLimitLeft(finalLimit);
                    med.setStatus(State.LOADED);
                    med.setDrone(drone);
                    medicationServiceRepository.save(med);
                });

                drone.setWeightLimitLeft(finalLimit);
                droneServiceRepository.save(drone);

                response.setResponseCode(ResponseCodes.SUCCESS);
                response.setResponseMessage("medication loaded successfully");
                response.setSerialNumber(request.getSerialNumber());
                log.info("medication loaded successfully");
            }

        } catch (Exception e) {
            log.info("Error Cause: {}", ExceptionUtils.getStackTrace(e));
            response.setResponseCode(ResponseCodes.INTERFACE_ERROR);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
            response.setSerialNumber(request.getSerialNumber());
        }

        log.info("Medication response: {}", response);
        return response;
    }

    private void logAndSetResponse(String message, String code, String serialNumber, LoadMedicationResponse.Response response) {
        log.info(message);
        response.setResponseMessage(message);
        response.setResponseCode(code);
        response.setSerialNumber(serialNumber);
    }

    public FetchAllLoadedMedicationResponse getLoadedMedications(String serialNumber) {
        log.info("serial number: " + serialNumber);
        FetchAllLoadedMedicationResponse response = new FetchAllLoadedMedicationResponse();

        Optional<Drone> droneOptional = droneServiceRepository.findById(serialNumber);
        if(!droneOptional.isPresent()){
            response.setResponseCode(ResponseCodes.INVALID_SERIAL_NUMBER);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INVALID_SERIAL_NUMBER));
            return response;
        }

        try{
        List<Medication> meds = medicationServiceRepository.findAllByDrone_SerialNumberAndStatus(serialNumber, LOADED);
        log.info("medications: " + Arrays.asList(meds));
        if(meds.isEmpty()) {
            response.setResponseCode(ResponseCodes.EMPTY_DRONE);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.EMPTY_DRONE));
        }else{
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.SUCCESS));
        }
        response.setMedicationList(meds);
        }
        catch (Exception e) {
            log.info("Error Cause :: "+ ExceptionUtils.getStackTrace(e));
            response.setResponseCode(ResponseCodes.INTERFACE_ERROR);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR));
        }
        log.info("loaded medications response: " + response);
        return response;
    }

    public DeliveryStatusResponse getDeliveryStatus(String dispatchNumber){
        DeliveryStatusResponse response = new DeliveryStatusResponse();
        try{
        Optional<DispatchLog> dispatch = dispatchLogRepository.findByDispatchNumber(dispatchNumber+ "D");
        if(dispatch.isPresent()){
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.MEDICATION_SENT));
            response.setDeliveryStatus(dispatch.get().getDeliveryStatus());
            response.setExpectedDeliveryTime(dispatch.get().getExpectedDeliveryTime());
        }else{
            Optional<DispatchLog> dispatch1 = dispatchLogRepository.findByDispatchNumber(dispatchNumber);
            response.setResponseCode(ResponseCodes.SUCCESS);
            response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.MEDICATION_DISPATCHED));
            response.setDeliveryStatus(dispatch1.get().getDeliveryStatus());
            response.setExpectedDeliveryTime(dispatch1.get().getExpectedDeliveryTime());
        }

        return response;
        }
        catch (Exception e) {
            log.info("Error Cause :: "+ ExceptionUtils.getStackTrace(e));
            throw new GenericException(ResponseCodes.INTERFACE_ERROR, ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR), null);
        }
    }

    public ConfirmationResponse confirmDelivery(DeliveryConfirmationRequest request){
        ConfirmationResponse response = new ConfirmationResponse();
        DispatchLog logger = new DispatchLog();
        LocalTime currentTime = LocalTime.now();
//        LocalTime actualDeliveryTime = request.getEstimatedTimeOfDelivery();

        try{
            List<Medication> list = medicationServiceRepository.findAllByDrone_SerialNumberAndStatus(request.getSerialNumber(), State.DELIVERED);
            Drone drone = droneServiceRepository.getById(request.getSerialNumber());
            Optional<DispatchLog> dispatchedItem = dispatchRepo.findByDispatchNumber(request.getDispatchNumber());
//            LocalTime actualDeliveryTime = dispatchedItem.get().getDispatchTime();
            if(drone.getState().equals(State.DELIVERED)){
                log.info("Delivery Successful, Return initiated");
                if(dispatchedItem != null ){
                    LocalTime actualDeliveryTime = dispatchedItem.get().getExpectedDeliveryTime();
                    LocalTime dispatchTime  = dispatchedItem.get().getDispatchTime();
                    long hours = dispatchTime.until(actualDeliveryTime, ChronoUnit.HOURS);
                    long minutes = dispatchTime.until(actualDeliveryTime, ChronoUnit.MINUTES);
                    long seconds = dispatchTime.until(actualDeliveryTime, ChronoUnit.SECONDS);

                    LocalTime now = LocalTime.now();
                    LocalTime timeNow = LocalTime.of(now.getHour(), now.getMinute(), now.getSecond());
                    LocalTime returnTime = timeNow.plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
                    if (drone.getState() != State.DELIVERED) {
                        response.setResponseCode(ResponseCodes.CANNOT_BE_INITIATED);
                        response.setDispatchNumber(request.getDispatchNumber());
                        response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.CANNOT_BE_INITIATED));
                        response.setSerialNumber(drone.getSerialNumber());
                        return response;
                    }else if(timeNow.isAfter(returnTime)){
                        response.setResponseCode(ResponseCodes.INVALID_TIME);
                        response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.INVALID_TIME));
                        response.setDispatchNumber(request.getDispatchNumber());
                        response.setSerialNumber(drone.getSerialNumber());
                        response.setExpectedReturnTime(returnTime);
                        return response;
                    }
                    drone.setState(State.RETURNING);
                    droneServiceRepository.save(drone);
                    log.info("Items Received, Request for drone return with serial number :: "+request.getSerialNumber()+" initiated");
                    response.setResponseCode(ResponseCodes.ITEMS_RECEIVED);
                    response.setResponseMessage(ResponseCodes.getErrorMessage(ResponseCodes.ITEMS_RECEIVED));
                    response.setDispatchNumber(request.getDispatchNumber());
                    response.setExpectedReturnTime(returnTime);
                    response.setSerialNumber(request.getSerialNumber());
                    List<LoadMedicationResponse.MedicationResp> medList = new ArrayList<>();
                    list.forEach(x->{
                        LoadMedicationResponse.MedicationResp med = new LoadMedicationResponse.MedicationResp();
                        med.setCode(x.getCode());
                        med.setImage(x.getImage());
                        med.setName(x.getName());
                        med.setWeight(FormatterUtil.decimalFormatter(x.getWeight()));
                        medList.add(med);
                    });
                    response.setReceivedMedications(medList);
                    CompletableFuture.supplyAsync(() ->{
                        returnDroneSchedular.returnDrone(drone, returnTime);
                        log.info("current time: {}", response);
                        return response;
                    }).thenApply(DispatchResponse -> {
                        while(timeNow.isBefore(returnTime)){
                            returnDroneSchedular.returnDrone(drone, returnTime);
                        }
                        return response;
                    });
                }
            }
            logger.setDroneModel(drone.getModel());
            logger.setSerialNumber(drone.getSerialNumber());
            logger.setTimestamp(new Timestamp(System.currentTimeMillis()));
            assert dispatchedItem != null;
            logger.setExpectedDeliveryTime(dispatchedItem.get().getExpectedDeliveryTime());
            logger.setExpectedReturnTime(response.getExpectedReturnTime());
            logger.setDeliveryStatus(State.DELIVERED);
            logger.setDispatchTime(LocalTime.now());
            logger.setDispatchNumber(request.getDispatchNumber() + "D");
            logger.setMedicationTotalWeight(0);
            dispatchRepo.save(logger);
            return response;
        }catch (Exception e) {
            log.error("Error occurred while Confirming Delivery: {}", ExceptionUtils.getStackTrace(e));
            throw new GenericException(ResponseCodes.INTERFACE_ERROR, ResponseCodes.getErrorMessage(ResponseCodes.INTERFACE_ERROR), null);
        }
    }
}

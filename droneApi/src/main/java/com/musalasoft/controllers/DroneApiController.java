package com.musalasoft.controllers;

import com.musalasoft.dto.*;
import com.musalasoft.services.serviceImplimentation.DroneService;
import com.musalasoft.services.serviceImplimentation.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Validated
@RequestMapping("/v1/drone-api")
@RequiredArgsConstructor
@RestController
public class DroneApiController {
    private final MedicationService medicationService;
    private final DroneService droneService;

    @PostMapping("/register-drone")
    public ResponseEntity<DroneRegistrationResponse> DroneRegistration(@Valid @RequestBody DroneRegistrationRequest request) {
        return ResponseEntity.ok().body(droneService.registerDrone(request));
    }

    @PostMapping("/load-medication")
    public ResponseEntity<LoadMedicationResponse.Response> loadMedication(@Valid @RequestBody LoadMedicationRequest.Request request) {
        return ResponseEntity.ok().body(medicationService.loadMedication(request));
    }

    @GetMapping(path = "/loaded-medications/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FetchAllLoadedMedicationResponse> fetchAllLoadedMedications(@PathVariable String serialNumber) {
        return ResponseEntity.ok().body(medicationService.getLoadedMedications(serialNumber));
    }

    @PostMapping(path = "/dispatch-drone", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DispatchResponse> dispatch(@Valid @RequestBody DispatchRequest request) {
        return ResponseEntity.ok().body(droneService.dispatchDrone(request));
    }

    @PostMapping(path = "/confirm-delivery", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ConfirmationResponse> dispatch(@Valid @RequestBody DeliveryConfirmationRequest request) {
        return ResponseEntity.ok().body(medicationService.confirmDelivery(request));
    }

    @GetMapping(path = "/get-delivery-status/{dispatchNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DeliveryStatusResponse getDeliveryStatus(@PathVariable String dispatchNumber) {
        return medicationService.getDeliveryStatus(dispatchNumber);
    }

    @GetMapping("/available-drones")
    public ResponseEntity<AvailableDronesResponse> isDroneAvailable() {
        return ResponseEntity.ok().body(droneService.droneAvailable());
    }

    @GetMapping(path = "/battery-capacity/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BatteryCapacityResponse> batteryCheck(@PathVariable String serialNumber) {
        return ResponseEntity.ok().body(droneService.batteryCapacity(serialNumber));
    }

    @GetMapping(path = "/fetch-all-drones", produces = MediaType.APPLICATION_JSON_VALUE)
    public AvailableDronesResponse getAllDrones() {
        return droneService.fetchAllDrones();
    }

    @PostMapping(path = "/charge-battery", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChargeBatteryResponse> chargeBattery(@Valid @RequestBody GenericDroneRequest request) {
        return ResponseEntity.ok().body(droneService.chargeBattery(request));
    }

    @GetMapping(path = "/get-drone-current-status/{serialNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DroneCurrentStatusResponse getDroneCurrentStatus(@PathVariable String serialNumber) {
        return droneService.getDroneCurrentStatus(serialNumber);
    }
}

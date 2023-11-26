# DRONE SERVICE APPLICATION
## Introduction
This is the development of a major new Technology set to be a disruptive force in the field of transportation: **"The Drone System"**.
It was Designed in accordance to the use case as was giving by the product owner **Musala-Soft**
Although there might be some assumptions as was advised in that task description in **README.md** located in this project directory.
## APPLICATION DESIGN DOCUMENTATION

### Database Connection:
An  In-Memory database (H2 database) was used for this Application as was advised in the task description. \
DRIVER CLASS: org.h2.Driver \
JDBC URL: jdbc:h2:mem:droneservicedb \
Username: sa \
Password: test

This Can be accessed in the browser locally as when the application is running with: \
http://localhost:8082/h2-console \
JDBC URL: jdbc:h2:mem:droneservicedb \
Username: sa \
Password: test

### This application consist of 2 main Services:
**1. The Drone Service** \
**2. The Medication Service**

### The Drone Service Comprises of the following APIs:

****1. Drone Registration:**** \
****2. Fetch All Available Drones:**** \
****3. Fetch Battery Capacity:**** \
****4. Fetch Drone Status:**** \
****5. Dispatch Drone:**** \
****6. Charge Drone:**** \
****7. Periodic task to check drones battery levels every 40 seconds. An audit-Log or an event-Log was created for this.****

****XX.**** ***Drone Registration:***\
This API is responsible for the registration of the drones. \
\
****Assumptions Made:**** \
**1.** On drone Registration, validated serial number is provided which is set as unique. \
**2.** On drone Registration the weight limit is derived from the drone Model type with is configurable in the property file. \
**3.** Property file Can be Externalised.

**Example:** \
Heavyweight drone has a weight limit of 500gr. \
Middleweight drone has a weight limit of 400gr. \
Cruiserweight drone has a weight limit of 300gr. \
Lightweight drone has a weight limit of 200gr. \
\
**4.** On drone Registration the drone state is set to IDLE. \
**5.** A Flag called batteryStatus was made available to check for Battery Status  of drone battery (charging, not charging and fully charged). \
**6.** On registration this flag is set NOT_CHARGING. 

**The drone registration is done by sending a POST request to the following endpoint:** \
URL: http://localhost:8082/v1/drone-api/register-drone \
\
**Validations:** 
1. Serial_Number cannot be an empty field
2. Serial_Number must be between 4 and 100 characters 
3. Battery Capacity On Registration cannot exceed 100% 
4. Drone Model must correspond with the known models provided Else, an exception will be returned 
5. Serial_Number cannot be registered twice (Duplicates not allowed)

\
Sample Payload: \
{ \
"serialNumber": "ADF67890568", \
"model": "Heavyweight", \
"batteryCapacity": 95 \
} \
\
Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Drone registered successfully", \
"serialNumber": "ADF67890568" \
}


****XX.**** ***Fetch All Available Drones:***

   This API is responsible for fetching all drones in their IDLE state as available drones.

  **This is achieved by sending a GET request to the following endpoint:** \
  URL: http://localhost:8082/v1/drone-api/available-drones

   Sample Response: \
   { \
   "responseCode": "000", \
    "responseMessage": "Available Drones Fetched Successfully",\
   "drones": [ \
   { \
   "serialNumber": "ADF67890568", \
   "model": "Heavyweight", \
   "weightLimit": 500gr., \
   "weightLimitLeft": 500gr., \
   "batteryCapacity": 95%, \
   "batteryStatus": "NOT_CHARGING", \
   "state": "IDLE" \
   }, \
   { \
   "serialNumber": "ADF67890569", \
   "model": "Cruiserweight", \
   "weightLimit": 300gr., \
   "weightLimitLeft": 300gr., \
   "batteryCapacity": 50, \
   "batteryStatus": "NOT_CHARGING", \
   "state": "IDLE" \
   } \
]\
}\
   ****XX.**** ***Fetch Battery Capacity:*** \
   This API is responsible for fetching the battery Status of a given drone at a time. \
   \
   ****Assumptions Made:**** \
   **1.** Battery capacity of a particular drone is fetched by its serial number.\
\
   **This is achieved by sending a GET request to the following endpoint:** \
  URL: http://localhost:8082/v1/drone-api/battery-capacity/{serialNumber} \
\
   Sample Response: \
   { \
   "responseCode": "000", \
   "responseMessage": "Success", \
   "serialNumber": " {serialNumber} ", \
   "batteryCapacity": 95%, \
   "batteryStatus": "NOT_CHARGING" \
   }\
\
   ****XX.**** ***Fetch Drone Status:*** \
   This API is responsible for fetching Drone Stratus.

****Assumptions Made:**** \
**1.** Fetching the Status of given drone is done by its serial number.\
\
**This is achieved by sending a GET request to the following endpoint:** \
http://localhost:8082/v1/drone-api/get-drone-current-status/{serialNumber} 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Drone Status Fetched Successfully", \
"droneModel": "Heavyweight", \
"batteryCapacity": 95%, \
"batteryStatus": "NOT_CHARGING", \
"droneCurrentState": "LOADED" \
}\
\
****XX.**** ***Dispatch Drone:*** \
This API is responsible for Dispatching drone for delivery after medications are being loaded successfully.

****Assumptions Made:**** 

**1.**  On Dispatching a Drone with a Particular serial number, the Drone State is set to DELIVERING and the battery status is set to DISCHARGING. 

**2.**  For Drone Dispatch, Estimated time of Delivery is required in LocalTime format, "23:28:00". This is so because, for this task we are unable to ascertain the distance or location.

**3.**  For This Dispatch a background 20 seconds scheduler service was implemented. This scheduler runs until the estimated time of delivery elapses. 

**4.**  Also, This background 20 seconds scheduler handles discharging of the battery level by 1% every 20 seconds of delivery until the estimated time of delivery elapses. 

**5.**  Once the estimated time of delivery is reached the Scheduler is terminated, Drone State set to DELIVERED and Battery status is set to NOT_CHARGING. 

**6.**  For Every Dispatch, the dispatch number is randomly generated and returned to the user in the response body to be used to initiate the api for confirmation of delivery. 

**This process is achieved by sending a POST request to the following endpoint:** \
http://localhost:8082/v1/drone-api/dispatch-drone

Sample Request payload: \
{ \
"serialNumber": "ADF67890568", \
"expectedArrivalTime": "22:57:10" \
}

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Dispatched Successfully", \
"dispatchNumber": "456788765f", \
"currentState": "DELIVERING", \
"expectedDeliveryTime": "22:57:10" \
} 

****XX.**** ***Charge Drone:*** \
This API is responsible for Charging the Drone until it is Fully Charged.

****Assumptions Made:**** \
**1.** When Charging the drone by its serial number the battery status is set to CHARGING. \
**2.** When The charge-battery end-point is executed a background scheduler picks this task and keeps incrementing the battery capacity by 1% every 3 seconds until the battery capacity is 100%. \
**3.** Once the battery capacity is 100 the Scheduler is terminated and the battery status is set to FULLY_CHARGED.
**4**  If the Scheduler is still active and the endpoint is prompted again it responds with a message "Drone Is Charging".\
**5.** If Battery is fully charged and the endpoint is prompted again it responds with a message "Drone Fully Charged". 

**This process is achieved by sending a POST request to the following endpoint:** \
http://localhost:8082/v1/drone-api/charge-battery

Sample Request payload: \
{ \
"serialNumber": "ADF67890565" \
}

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Success",\
"serialNumber": "ADF67890565", \
"status": "CHARGING" \
} 

### The Medication Service Comprises the following APIs:

****1. Load Medication:**** \
****2. Fetch Loaded Medication:**** \
****3. Fetch Delivery Status:**** \
****4. Confirmation Of Delivery:**** \
\
****XX.**** ***Load Medication:*** \
This API is responsible for Loading a Drone with a List of medications. 

****Assumptions Made:**** \
**1.** On Loading the drone a particular the drone state is set to LOADING. \
**2.** Once it is set to loading, performing loading on this drone is not possible. \
**3.** Drone cannot be loaded while its in Charging state. \
**4.** Also, After Loading medications the Drone State is set to LOADED and loading operation can be only performed again if the weight limit for this drone has not being exceeded and if the drone state has not set to DELIVERING. Else, the drone can only be loaded again when The state is returned to IDLE. \
\
**This process is achieved by sending a POST request to the following endpoint:** \
http://localhost:8082/v1/drone-api/load-medication

Sample Request payload: \
{ \
"serialNumber": "ADF67890569", \
"medications": [ \
{ \
"name": "paracetamol", \
"weight": 100, \
"code": "WS_234578", \
"image": "34567865JK" \
}, \
{ \
"name": "pain-killers", \
"weight": 100, \
"code": "WS_2345JL", \
"image": "34567865DY" \
}, \
{ \
"name": "cough-syrup", \
"weight": 200, \
"code": "WS_2345JK", \
"image": "34567865BN" \
} \
] \
}


Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "medication loaded successfully", \
"loaded_drones_serial_number": "ADF67890569" \
} 


****XX.**** ***Fetch Loaded Medication:*** \
This API is responsible for Fetching the List of Medications that are loaded on a given drone.

****Assumptions Made:**** \
**1.** On Fetching the loaded medication the Medication table in the database is filtered by drone's serial number.

**This process is achieved by sending a GET request to the following endpoint:** \
http://localhost:8082/v1/drone-api/get-loaded-medication/{serialNumber}


Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Success", \
"medicationList": [ \
{ \
"name": "paracetamol", \
"weight": 100, \
"code": "WS_234578", \
"image": "34567865JK", \
"status": "LOADED" \
}, \
{ \
"name": "pain-killers", \
"weight": 100, \
"code": "WS_2345JL", \
"image": "34567865DY", \
"status": "LOADED" \
}, \
{ \
"name": "cough-syrup", \
"weight": 300, \
"code": "WS_2345JK", \
"image": "34567865BN", \
"status": "LOADED" \
} \
] \
}

****XX.**** ***Fetch Delivery Status:*** \
This API is responsible for Fetching the Delivery Status of a given dispatched Medication.

****Assumptions Made:**** \
**1.** On Fetching the delivery status the Dispatch-Log table in the database is filtered by dispatch number. \
**2.** The Delivery Status is determined by Confirmation of Delivery. \
**3.** If The Delivery has not being confirmed Status is set DELIVERING and the response to the user is "Medications Dispatched".
**4.** Status is set to DELIVERED once the Delivery has being confirmed and the response to the user is "Medications Delivered Successfully".

**This process is achieved by sending a GET request to the following endpoint:** \
http://localhost:8082/v1/drone-api/get-delivery-status/{dispatchNumber}

Sample Response: \
Before confirmation of Delivery: \
{ \
"responseCode": "000", \
"responseMessage": "Medications Dispatched", \
"expectedDeliveryTime": "15:51:10", \
"deliveryStatus": "DELIVERING" \
}

Sample Response: \
After confirmation of Delivery: \
{ \
"responseCode": "000", \
"responseMessage": "Medications Delivered Successfully", \
"expectedDeliveryTime": "15:51:10", \
"deliveryStatus": "DELIVERED" \
}

****XX.**** ***Confirmation Of Delivery:*** \
This API is responsible for Confirming that the dispatched items had being receivedS successfully.

****Assumptions Made:**** \
**1.** On Confirmation of Delivery the Dispatch-Log table in the database is filtered by dispatch number passed in the request body. 

**2.** When the endpoint for Confirmation of delivery is prompted, the actual time of Delivery is set to LocalTime.now(). 

**3.** The Delivery Status is set to DELIVERED once the confirmation of delivery is Endpoint is called. 

**4.** After confirmation,the drone state is set to RETURNING. 

**5.** During this RETURN period, A scheduler picks task and runs until the return time set elapses. 

**6.** Return time equivalent to LocalTime.now() plus the estimated delivery time. 

**7.** Also, the battery status is set to DISCHARGING as it keeps reducing by 1% every 20 seconds of its journey until the expected return time is reached. 

**8.** The drone state is set to IDLE and the battery status set to NOT_CHARGING once it returns home.


\
This process is achieved by sending a POST request to the following endpoint: \
http://localhost:8082/v1/drone-api/confirm-delivery


\
Sample Request payload: \
{ \
"serialNumber": "ADGH123457", \
"dispatchNumber": "db4958c3" \
}

Sample Response: \
{ \
"responseCode": "00", \
"responseMessage": "Items Received Successfully", \
"serialNumber": "ADGH123457", \
"dispatchNumber": "db4958c3", \
"expectedReturnTime": "23:30:44", \
"receivedMedications": [ \
{ \
"name": "ampicilene", \
"weight": 200gr., \
"code": "FO4", \
"image": "wertyuioiugfydtsrdfghjghkjjhgfhdfjgkhthre" \
}, \
{ \
"name": "funbact-A", \
"weight": 200gr., \
"code": "FO23", \
"image": "wertyuqwetryuiituryesdfyugrdtfhj" \
} \
] \
} 
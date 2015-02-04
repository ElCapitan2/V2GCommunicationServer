/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver;


import v2gcommunication.commonclasses.requests.RequestManagement;
import v2gcommunication.commonclasses.tasks.TaskManagement;
import v2gcommunicationserver.serverfunctions.*;

/**
 *
 * @author Alexander Forell
 * 
 * Class is handlig basic server functionality
 *  - start up
 *  - initializing sockets and ports
 *  - shut down
 */
public class V2GCommunicationServer {

    /**
     * @author Alexander Forell
     * @param args the command line arguments are not used
     * Function starts up server
     */
    public static void main(String[] args) {
        //Start Database Connection 
        DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
        // Instanciate Authenticate
        Authenticate authenticate = new Authenticate();
        // Instanciate TaskManagement
        TaskManagement vehicleTasks = new TaskManagement();
        // Instanciate RequestManagement
        RequestManagement requestManagement = new RequestManagement();
        /**
         * Add task workers
         */
        requestManagement.addTaskWorker(CreateUser.class);
        requestManagement.addTaskWorker(GetUsers.class);
        requestManagement.addTaskWorker(DeleteUser.class);
        requestManagement.addTaskWorker(GetUserRights.class);
        requestManagement.addTaskWorker(UpdatePassword.class);
        requestManagement.addTaskWorker(UpdateUserrights.class);
        requestManagement.addTaskWorker(AddVehicle.class);
        requestManagement.addTaskWorker(GetVehicles.class);
        requestManagement.addTaskWorker(DeleteVehicle.class);
        requestManagement.addTaskWorker(InitiateRequestVehicleData.class);
        requestManagement.addTaskWorker(GetStoredFunctions.class);
        requestManagement.addTaskWorker(DownloadData.class);
        // Start SessionManagement
        SessionManagement vehicleSessions = new SessionManagement(25001);
        // Set connections between classes
        requestManagement.setAddRequest(vehicleSessions);
        requestManagement.setStartTaskTransmit(vehicleTasks);
        vehicleSessions.setNewSessionGetTasksListener(vehicleTasks);
        vehicleSessions.setNewSessionGetRequestsListener(requestManagement);
        vehicleSessions.setTaskTransmittedListener(vehicleTasks);
        vehicleSessions.setNewReceiveTaskListener(vehicleTasks);
        vehicleSessions.setRequestReceivedListener(requestManagement);
        vehicleSessions.setRequestTransmittedListener(requestManagement);
        vehicleSessions.setAuthenticateVINListener(authenticate);
        StoreData storeData = new StoreData(vehicleTasks);
        vehicleTasks.setNewTaskToStore(storeData);
        vehicleTasks.setNewTransmitTask(vehicleSessions);
        // Start datastrorer thread
        storeData.start();
    }
    
}

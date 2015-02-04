/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver.serverfunctions;

import java.util.ArrayList;
import v2gcommunication.commonclasses.requests.Request;
import v2gcommunication.commonclasses.requests.RequestType;
import v2gcommunication.commonclasses.tasks.ParameterSet;
import v2gcommunication.commonclasses.requests.AddRequest;
import v2gcommunication.commonclasses.tasks.StartTaskTransmit;
import v2gcommunication.commoninterfaces.TaskWorker;

/**
 * Server side implementation to send a request for data to a vehicle. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class RequestVehicleData implements TaskWorker{
    private Request request;
    private StartTaskTransmit taskReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * For the server side the run method is not used.
     * 
     */
    @Override
    public void run() {
    }

    /**
     * Method defined in Taskworker is used for a the runMethod to reply using
     * Requests or Tasks.
     * 
     * @param requestProcessed  The Request which is currently handled
     * @param requestReply      The Request used to reply
     * @param taskReply         The Task used to reply.
     */
    @Override
    public void inputForRunnable(Request requestProcessed, AddRequest requestReply, StartTaskTransmit taskReply) {
        this.request = requestProcessed;
        this.taskReply = taskReply;
    }

    /**
     * Static Mehtod to get a Request
     * 
     * @param VIN                   VIN of the vehicle
     * @param requestID             ID of the request
     * @param vehicleFunctions      Vehicle Functions to be collected
     * @param intervall             Intervall [ms]
     * @param iterations            Iterations [-]
     * @return                      Request to be transmitted
     */
    public static Request getRequestVehicleDataRequest(String VIN, 
            String requestID, ArrayList<String> vehicleFunctions, 
            int intervall, int iterations){
        ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
        
        // Add Functions to Request
        for (String vehicleFunction:vehicleFunctions){
            ParameterSet psFunction = new ParameterSet();
            psFunction.parameterName = "functionName";
            psFunction.parameterType = String.class.getName();
            psFunction.parameterValue = vehicleFunction;
            psFunction.timeStamp = null;
            parameters.add(psFunction);
        }
        
        //Create Parameterset for intervall
        ParameterSet psIntervall = new ParameterSet();
        psIntervall.parameterName="intervall";
        psIntervall.parameterType=int.class.getName();
        psIntervall.parameterValue=String.valueOf(intervall);
        psIntervall.timeStamp = null;
        parameters.add(psIntervall);
        
        //Create Parameter set for vehName
        ParameterSet psVehname = new ParameterSet();
        psVehname.parameterName="iterations";
        psVehname.parameterType=int.class.getName();
        psVehname.parameterValue=String.valueOf(iterations);
        psVehname.timeStamp = null;
        parameters.add(psVehname);
        
        
        Request createUserRequest = new Request(RequestType.REQUEST, VIN, 
                requestID ,"RequestVehicleData", parameters);
        return createUserRequest;
    }
    
}

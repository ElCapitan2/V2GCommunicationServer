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
import v2gcommunicationserver.DatabaseConnection;

/**
 * Server side implementation to process a request read data from vehicles.
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class InitiateRequestVehicleData implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and send out requests to the specified vehicles.
     * 
     * It will build Answer Request, whether the requests are successfully 
     * added to vehicles 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            parameters.addAll(request.getParameterSet());
            ArrayList<String> vehNames = new ArrayList<String>();
            ArrayList<String> functionNames = new ArrayList<String>();
            int intervall = 0;
            int iterations = 0;
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("vehname") && para.parameterType.equals(String.class.getName())){
                    vehNames.add(para.parameterValue);
                }
                if (para.parameterName.equals("functionName") && para.parameterType.equals(String.class.getName())){
                    functionNames.add(para.parameterValue);
                }
                if (para.parameterName.equals("intervall") && para.parameterType.equals(int.class.getName())){
                    intervall = Integer.parseInt(para.parameterValue);
                }
                if (para.parameterName.equals("iterations") && para.parameterType.equals(int.class.getName())){
                    iterations = Integer.parseInt(para.parameterValue);
                }
            }
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (String vehname:vehNames){
                String vin = conn.getVIN(vehname);
                Request re = RequestVehicleData.getRequestVehicleDataRequest(vin, 
                        request.getRequestID(), functionNames, intervall, iterations);
                requestReply.addRequest(re);
            }
            
            Request answer = buildAnswerRequest(request.getUserNameVIN(), request.getRequestID(), true);
            requestReply.addRequest(answer);
        }
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
        this.requestReply = requestReply;
    }
 
    /**
     * Builds an Answer request indicating whehter the vehicle was successfully
     * added
     * 
     * @param username              String username to send the Request to
     * @param ID                    Request ID
     * @param requestInitiated      Success of creating requests
     * @return                      Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, boolean requestInitiated){
        ArrayList<ParameterSet> paramset = new ArrayList<ParameterSet>();
        
        
        ParameterSet psAnswer = new ParameterSet();
        psAnswer.parameterName = "requestInitiated";
        psAnswer.parameterType = boolean.class.getName();
        psAnswer.parameterValue = String.valueOf(requestInitiated);
        psAnswer.timeStamp = null;
        paramset.add(psAnswer);
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "InitiateRequestVehicleData", paramset);
        return answer;
    }
}

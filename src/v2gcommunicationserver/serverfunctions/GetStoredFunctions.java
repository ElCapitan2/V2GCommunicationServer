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
 * Server side implementation to process a request functions stored in database 
 * for a list of vehicles. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class GetStoredFunctions implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and read the stored functions.
     * 
     * It will build Answer Request, including a list of the stored functions. 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            ArrayList<String> vehnames = new ArrayList<String>();
            parameters.addAll(request.getParameterSet());
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("vehname") && para.parameterType.equals(String.class.getName())){
                    vehnames.add(para.parameterValue);
                }
            }
            ArrayList<String> functions = conn.getStoredFunctions(vehnames);
            Request answer = buildAnswerRequest(request.getUserNameVIN(), 
                    request.getRequestID(), functions);
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
     * Builds an Answer request including the stored functions.
     * 
     * @param username      String username to send the Request to
     * @param ID            Request ID
     * @param functions     List of stored functions
     * @return              Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, ArrayList<String> functions){
        ArrayList<ParameterSet> paramset = new ArrayList<ParameterSet>();
        
        for (String function:functions){
            ParameterSet psAnswer = new ParameterSet();
            psAnswer.parameterName = "function";
            psAnswer.parameterType = String.class.getName();
            psAnswer.parameterValue = function;
            psAnswer.timeStamp = null;
            paramset.add(psAnswer);
        }
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "GetStoredFunctions", paramset);
        return answer;
    }
    
}

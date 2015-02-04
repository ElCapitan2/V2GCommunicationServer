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
 * Server side implementation to process a request get users. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class GetUsers implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection return a list of users
     * 
     * It will build Answer Request, including a list of users. 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<String> usernames = new ArrayList<String>();
            DatabaseConnection conn = DatabaseConnection.getInstance();
            
            usernames = conn.getUsers();
                    
            Request answer = buildAnswerRequest(request.getUserNameVIN(), 
                    request.getRequestID(), usernames);
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
     * Builds an Answer request inlcuding a list of users
     * 
     * @param username      String username to send the Request to
     * @param ID            Request ID
     * @param usernames     List of usernames
     * @return              Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, ArrayList<String> usernames){
        ArrayList<ParameterSet> paramset = new ArrayList<ParameterSet>();
        
        for (String user:usernames){
            ParameterSet psAnswer = new ParameterSet();
            psAnswer.parameterName = "username";
            psAnswer.parameterType = String.class.getName();
            psAnswer.parameterValue = user;
            psAnswer.timeStamp = null;
            paramset.add(psAnswer);
        }
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "GetUsers", paramset);
        return answer;
    }
}

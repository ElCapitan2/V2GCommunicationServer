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
 * Server side implementation to process a request to delete a user. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class DeleteUser implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and delete a user from the Database
     * 
     * It will build an answer Request, whether the user was successfully 
     * deleted. 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            parameters.addAll(request.getParameterSet());
            String username=null;
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("username") && para.parameterType.equals(String.class.getName())){
                    username = para.parameterValue;
                }
            }
            boolean deleted;
            deleted = conn.deleteUser(username);
            Request answer = buildAnswerRequest(request.getUserNameVIN(), request.getRequestID(), deleted, username);
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
     * Builds an Answer request indicating whehter the user was successfully
     * deleted.
     * 
     * @param username          String username to send the Request to
     * @param ID                Request ID
     * @param deleted           Success of deletion of user 
     * @param deletedUser       Name of user
     * @return                  Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, boolean deleted, String deletedUser){
        ParameterSet psAnswer = new ParameterSet();
        psAnswer.parameterName = "userDeleted";
        psAnswer.parameterType = boolean.class.getName();
        psAnswer.parameterValue = String.valueOf(deleted);
        psAnswer.timeStamp = null;
        ParameterSet psUser = new ParameterSet();
        psUser.parameterName = "nameOfUserDeleted";
        psUser.parameterType = String.class.getName();
        psUser.parameterValue = deletedUser;
        psUser.timeStamp = null;
        
        
        ArrayList<ParameterSet> paramAnswer = new ArrayList<ParameterSet>();
        paramAnswer.add(psAnswer);
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "DeleteUser", paramAnswer);
        return answer;
    }
}

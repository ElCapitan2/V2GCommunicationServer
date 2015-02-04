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
import v2gcommunication.commonclasses.userrights.UserPrivilage;
import v2gcommunication.commonclasses.requests.AddRequest;
import v2gcommunication.commonclasses.tasks.StartTaskTransmit;
import v2gcommunication.commoninterfaces.TaskWorker;
import v2gcommunicationserver.DatabaseConnection;

/**
 * Server side implementation to process a request to get userrights for a 
 * certain user. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class GetUserRights implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and return the rights of the user
     * 
     * It will build Answer Request, including the rights of the user. 
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
            UserPrivilage userPrivilage = conn.getUserRights(username);
            Request answer = buildAnswerRequest(request.getUserNameVIN(), request.getRequestID(), userPrivilage);
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
     * Builds an Answer request inlcuding the user rights
     * 
     * @param username          String username to send the Request to
     * @param ID                Request ID
     * @param userPrivilage     privilages of the users
     * @return                  Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, UserPrivilage userPrivilage){
        ParameterSet psUserName = new ParameterSet();
        psUserName.parameterName = "username";
        psUserName.parameterType = String.class.getName();
        psUserName.parameterValue = String.valueOf(userPrivilage.username);
        psUserName.timeStamp = null;
        
        ParameterSet psCanModifyUsers = new ParameterSet();
        psCanModifyUsers.parameterName = "canModifyUsers";
        psCanModifyUsers.parameterType = boolean.class.getName();
        psCanModifyUsers.parameterValue = String.valueOf(userPrivilage.canModifyUsers);
        psCanModifyUsers.timeStamp = null;
        
        ParameterSet psCanModifyVehicles = new ParameterSet();
        psCanModifyVehicles.parameterName = "canModifyVehicles";
        psCanModifyVehicles.parameterType = boolean.class.getName();
        psCanModifyVehicles.parameterValue = String.valueOf(userPrivilage.canModifyVehicles);
        psCanModifyVehicles.timeStamp = null;
        
        ParameterSet psCanSendRequest = new ParameterSet();
        psCanSendRequest.parameterName = "canSendRequest";
        psCanSendRequest.parameterType = boolean.class.getName();
        psCanSendRequest.parameterValue = String.valueOf(userPrivilage.canSendRequests);
        psCanSendRequest.timeStamp = null;
        
        ArrayList<ParameterSet> paramAnswer = new ArrayList<ParameterSet>();
        paramAnswer.add(psUserName);
        paramAnswer.add(psCanModifyUsers);
        paramAnswer.add(psCanModifyVehicles);
        paramAnswer.add(psCanSendRequest);
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "GetUserRights", paramAnswer);
        return answer;
    }
}

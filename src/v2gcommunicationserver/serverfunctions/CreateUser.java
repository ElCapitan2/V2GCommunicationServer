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
 * Server side implementation to process a request to add a user. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class CreateUser implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and add a user to the Database
     * 
     * It will build Answer Request, whether the vehicle was successfully added. 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            parameters.addAll(request.getParameterSet());
            String newUsername=null;
            String password=null;
            boolean canModifyUsers= false;
            boolean canModifyVehicles = false;
            boolean canSendRequest = false;
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("newUsername") && para.parameterType.equals(String.class.getName())){
                    newUsername = para.parameterValue;
                }
                if (para.parameterName.equals("password") && para.parameterType.equals(String.class.getName())){
                    password = para.parameterValue;
                }
                if (para.parameterName.equals("canModifyUsers") && para.parameterType.equals(boolean.class.getName())){
                    canModifyUsers = Boolean.parseBoolean(para.parameterValue);
                }
                if (para.parameterName.equals("canModifyVehicles") && para.parameterType.equals(boolean.class.getName())){
                    canModifyVehicles = Boolean.parseBoolean(para.parameterValue);
                }
                if (para.parameterName.equals("canSendRequest") && para.parameterType.equals(boolean.class.getName())){
                    canSendRequest = Boolean.parseBoolean(para.parameterValue);
                }
                
                
            }
            boolean userCreated;
            userCreated = conn.createUser(newUsername, password, 
                    canModifyUsers, canModifyVehicles, canSendRequest);
            Request answer = buildAnswerRequest(request.getUserNameVIN(), 
                    request.getRequestID(), userCreated, newUsername);
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
     * added
     * 
     * @param username              String username to send the Request to
     * @param ID                    Request ID
     * @param userCreated           Success of insertion of user
     * @param nameOfCreatedUser     Name of user added
     * @return                      Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, boolean userCreated, String nameOfCreatedUser){
        ParameterSet psAnswer = new ParameterSet();
        psAnswer.parameterName = "userCreated";
        psAnswer.parameterType = boolean.class.getName();
        psAnswer.parameterValue = String.valueOf(userCreated);
        psAnswer.timeStamp = null;
        
        ParameterSet psNameOfCreateUser = new ParameterSet();
        psNameOfCreateUser.parameterName = "nameOfCreatedUser";
        psNameOfCreateUser.parameterType = String.class.getName();
        psNameOfCreateUser.parameterValue = nameOfCreatedUser;
        psNameOfCreateUser.timeStamp = null;
        
        
        
        ArrayList<ParameterSet> paramAnswer = new ArrayList<ParameterSet>();
        
        paramAnswer.add(psAnswer);
        paramAnswer.add(psNameOfCreateUser);
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "CreateUser", paramAnswer);
        return answer;
    }
}

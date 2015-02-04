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
 * Server side implementation to process a request update the password of a user. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class UpdatePassword implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection and update the password of the user.
     * 
     * It will build Answer Request, whether password was reset successfully 
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            parameters.addAll(request.getParameterSet());
            String newUsername=null;
            String password=null;
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("nameOfUser") && para.parameterType.equals(String.class.getName())){
                    newUsername = para.parameterValue;
                }
                if (para.parameterName.equals("password") && para.parameterType.equals(String.class.getName())){
                    password = para.parameterValue;
                }
            }
            boolean passwordChanged;
            passwordChanged = conn.updatePassword(newUsername, password);
            Request answer = buildAnswerRequest(request.getUserNameVIN(), 
                    request.getRequestID(), passwordChanged, newUsername);
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
     * @param passwordUpdated       Password update
     * @param nameOfUser            Name of user
     * @return                      Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, boolean passwordUpdated, String nameOfUser){
        ParameterSet psAnswer = new ParameterSet();
        psAnswer.parameterName = "passwordUpdated";
        psAnswer.parameterType = boolean.class.getName();
        psAnswer.parameterValue = String.valueOf(passwordUpdated);
        psAnswer.timeStamp = null;
        
        ParameterSet psNameOfUser = new ParameterSet();
        psNameOfUser.parameterName = "nameOfUser";
        psNameOfUser.parameterType = String.class.getName();
        psNameOfUser.parameterValue = nameOfUser;
        psNameOfUser.timeStamp = null;
        
        ArrayList<ParameterSet> paramAnswer = new ArrayList<ParameterSet>();
        
        paramAnswer.add(psAnswer);
        paramAnswer.add(psNameOfUser);
        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "UpdatePassword", paramAnswer);
        return answer;
    }
}

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
import v2gcommunication.commonclasses.tasks.Task;
import v2gcommunication.commonclasses.requests.AddRequest;
import v2gcommunication.commonclasses.tasks.StartTaskTransmit;
import v2gcommunication.commoninterfaces.TaskWorker;
import v2gcommunicationserver.DatabaseConnection;

/**
 * Server side implementation to process a request download vehicle data. 
 * 
 * Class implements Taskworker which extends Runnable.
 * 
 * The run method performs the server actions.
 * 
 * @author Alexander Forell
 */
public class DownloadData implements TaskWorker{
    private Request request;
    private AddRequest requestReply;
    StartTaskTransmit taskReply;
    
    /**
     * Run mehthod must be overridden as it is defined in TaskWorker.
     * 
     * The method will read the Request, get an instange of the 
     * DatabaseConnection download the requested Data from the database
     * 
     * It will send the required data through tasks to the client build 
     * an Answer Request in which the corresponding task ids are included.
     */
    @Override
    public void run() {
        if (request.getRequestType()==RequestType.REQUEST){
            ArrayList<ParameterSet> parameters = new ArrayList<ParameterSet>();
            ArrayList<String> vehnames = new ArrayList<String>();
            ArrayList<String> functionNames = new ArrayList<String>();
            String FileName = null;
            parameters.addAll(request.getParameterSet());
            DatabaseConnection conn = DatabaseConnection.getInstance();
            for (ParameterSet para:parameters){
                if (para.parameterName.equals("vehname") && para.parameterType.equals(String.class.getName())){
                    vehnames.add(para.parameterValue);
                }
                if (para.parameterName.equals("function") && para.parameterType.equals(String.class.getName())){
                    functionNames.add(para.parameterValue);
                }
                if (para.parameterName.equals("fileName") && para.parameterType.equals(String.class.getName())){
                    FileName = para.parameterValue;
                }
            }
            ArrayList<Task> transmitData = new ArrayList<Task>();
            
            transmitData = conn.getData(request.getUserNameVIN(),vehnames, functionNames, taskReply);
            
            
            
            Request answer = buildAnswerRequest(request.getUserNameVIN(), 
                    request.getRequestID(), transmitData, FileName);
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
        this.taskReply = taskReply;
        
    }

    /**
     * Builds an Answer request including the taskids of the transmitted data
     * 
     * @param username      String username to send the Request to
     * @param ID            Request ID
     * @param tasks         List of task being transferred to client
     * @param fileName      filename in which data shall be stored
     * @return              Request to be transmitted
     */
    private Request buildAnswerRequest(String username, String ID, 
            ArrayList<Task> tasks, String fileName){
        ArrayList<ParameterSet> paramset = new ArrayList<ParameterSet>();
        
        for (Task task:tasks){
            ParameterSet psAnswer = new ParameterSet();
            psAnswer.parameterName = "task";
            psAnswer.parameterType = String.class.getName();
            psAnswer.parameterValue = task.getTaskID();
            psAnswer.timeStamp = null;
            paramset.add(psAnswer);
        }
        
        ParameterSet psAnswer = new ParameterSet();
        psAnswer.parameterName = "fileName";
        psAnswer.parameterType = String.class.getName();
        psAnswer.parameterValue = fileName;
        psAnswer.timeStamp = null;
        paramset.add(psAnswer);

        
        Request answer =  new Request(RequestType.ANSWER, username, ID,
                    "DownloadData", paramset);
        return answer;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import v2gcommunication.commonclasses.encryption.DESDecode;
import v2gcommunication.commonclasses.encryption.DESEncode;
import v2gcommunication.commonclasses.requests.Request;
import v2gcommunication.commonclasses.sessions.Session;
import v2gcommunication.commonclasses.tasks.Task;
import v2gcommunication.commonclasses.transmission.ConvertJSON;
import v2gcommunication.commonclasses.transmission.ReceiveProtocol;
import v2gcommunication.commonclasses.transmission.TransmitProtocol;
import v2gcommunication.commonclasses.requests.AddRequest;
import v2gcommunication.commonclasses.sessions.AuthenticateAccess;
import v2gcommunication.commonclasses.encryption.Decode;
import v2gcommunication.commonclasses.encryption.Encode;
import v2gcommunication.commonclasses.transmission.MessageBuilder;
import v2gcommunication.commonclasses.tasks.NewReceiveTask;
import v2gcommunication.commonclasses.tasks.NewSessionGetTasks;
import v2gcommunication.commonclasses.transmission.ReadMessage;
import v2gcommunication.commonclasses.tasks.DataTransmitted;
import v2gcommunication.commonclasses.requests.NewSessionGetRequests;
import v2gcommunication.commonclasses.sessions.NewTransmitTask;
import v2gcommunication.commonclasses.requests.RequestReceived;
import v2gcommunication.commonclasses.requests.RequestTransmitted;
import v2gcommunication.commonclasses.transmission.WriteMessage;

/**
 * SessionManagement for the Server side.
 * 
 * This class implements a runnable in which a Server Sockets waits for 
 * incoming connections. Once a vehicle or a client connects this class will
 * start a new session.
 * 
 * It provides interfaces to pass tasks requests on to sessions and inform
 * Task and Request Mangegment of incoming tasks
 * 
 * @author Alexander Forell
 */
public class SessionManagement implements NewTransmitTask, AddRequest{
    private final ExecutorService executor;
    private final ArrayList<Session> sessions;
    private final Encode enc; 
    private final Decode dec;
    private final MessageBuilder messageBuilder;
    private final ReadMessage readMessage;
    private final WriteMessage writeMessage;
    private NewReceiveTask newReceiveTask;
    private DataTransmitted taskTransmitted;
    private RequestTransmitted requestTransmitted;
    private RequestReceived requestReceived;
    private AuthenticateAccess authenticateVIN;
    private NewSessionGetTasks newSessionGetTasks;
    private NewSessionGetRequests newSessionGetRequests;
    private final int portNumber;
    private final Lock lockSessionList;
    
    /**
     * Constructor initializes fields.
     * 
     * Uses the portnumber to listen on the specified port
     * 
     * @param portNumber Portnumber server is listening on
     */
    SessionManagement(int portNumber){
        this.executor = Executors.newCachedThreadPool();
        this.sessions = new ArrayList<Session>();
        this.enc = new DESEncode();
        this.dec = new DESDecode();
        this.messageBuilder = new ConvertJSON();
        this.readMessage = new ReceiveProtocol();
        this.writeMessage = new TransmitProtocol();
        this.portNumber = portNumber;
        this.executor.execute(ListenForVehcileConnections);
        this.lockSessionList = new ReentrantLock();
    }
    
    /**
     * Starts a new Session 
     * 
     * @param socket Socket given by ServerSocket
     */
    private void newSession(Socket socket){
        try {
            Session session = new Session(socket, executor, enc, dec, messageBuilder,
                    readMessage, writeMessage, newReceiveTask, taskTransmitted, 
                    requestTransmitted, requestReceived, authenticateVIN, 
                    newSessionGetTasks, newSessionGetRequests);
            lockSessionList.lock();
            try {
                sessions.add(session);
            } finally {
                lockSessionList.unlock();
            }
        } catch (IOException ex) {
            Logger.getLogger(SessionManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Set Listener for new receiving Task
     * 
     * @param newReceiveTask 
     */
    public void setNewReceiveTaskListener(NewReceiveTask newReceiveTask){
        this.newReceiveTask = newReceiveTask;
    }
    
    /**
     * Set Listener for a Task being completly Transmitted
     * 
     * @param taskTransmitted Object being informed if a Task was transmitted
     */
    public void setTaskTransmittedListener(DataTransmitted taskTransmitted){
        this.taskTransmitted = taskTransmitted;
    }
    
    /**
     * Set Object being used to authenticate VIN or username
     * 
     * @param authenticateVIN Object being used to authenticate VIN or username
     */
    public void setAuthenticateVINListener(AuthenticateAccess authenticateVIN){
        this.authenticateVIN = authenticateVIN;
    }
    
    /**
     * Set Object Buffering Tasks.
     * 
     * @param newSession 
     */
    public void setNewSessionGetTasksListener(NewSessionGetTasks newSession){
        this.newSessionGetTasks = newSession;
    }
    
    /**
     * Set Object Buffering Requests.
     * 
     * @param newSession 
     */
    public void setNewSessionGetRequestsListener(NewSessionGetRequests newSession){
        this.newSessionGetRequests = newSession;
    }
    /**
     * Set Listener for a Request being completly Transmitted
     * @param requestTransmitted Object being informed if a Request was transmitted
     */
    public void setRequestTransmittedListener(RequestTransmitted requestTransmitted){
        this.requestTransmitted = requestTransmitted;
    }
    /**
     * Set Listener for incoming Requests.
     * 
     * @param requestReceived 
     */
    public void setRequestReceivedListener(RequestReceived requestReceived){
        this.requestReceived = requestReceived;
    }
    
    
    
    /**
     * Runnable waiting for connecitons
     */
    Runnable ListenForVehcileConnections = new Runnable(){
        
        @Override
        public void run() {
            ServerSocket serverSocket;

            try {
                serverSocket = new ServerSocket(portNumber);
                System.out.println("Listening on port " + portNumber);
                while (true){
                    
                    newSession(serverSocket.accept());
                }
            } catch (IOException ex) {
                Logger.getLogger(SessionManagement.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Could not listen on port " + portNumber);
                System.exit(-1);
            } 
        }
        
    };

    @Override
    public void addTransmitTask(Task ta) {
        lockSessionList.lock();
        try {
            for (Session se:sessions){
                if (ta.getVIN().equals(se.getVIN())){
                    se.addTransmitTask(ta);
                }
            }
        } finally {
            lockSessionList.unlock();
        }
    }

    @Override
    public void addRequest(Request request) {
        lockSessionList.lock();
        try {
            for (Session se:sessions){
                if (request.getUserNameVIN().equals(se.getVIN())){
                    se.addRequest(request);
                }
            }
        } finally {
            lockSessionList.unlock();
        }
    }
    
    
}

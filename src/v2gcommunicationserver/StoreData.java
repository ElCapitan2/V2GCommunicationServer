/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import v2gcommunication.commonclasses.tasks.ParameterSet;
import v2gcommunication.commonclasses.tasks.Task;
import v2gcommunication.commonclasses.tasks.TaskState;
import v2gcommunication.commonclasses.sessions.DataAdded;
import v2gcommunication.commonclasses.tasks.DataStored;
import v2gcommunication.commonclasses.sessions.GetTasks;
import v2gcommunication.commoninterfaces.NewTaskToStore;

/**
 * Class extends Thread is Stroing data into the database.
 * 
 * @author Alexander Forell
 */
public class StoreData extends Thread implements GetTasks, NewTaskToStore, DataAdded{
    private final DatabaseConnection dbconn;
    private final DataStored dataStored;
    // List of Tasks.
    private ArrayList<Task> receiveTasks;
    private Lock lock;
    Condition waitForData;
    
    /**
     * Constructor, 
     * sets DataStored dataStored is informed, if data was stored completly.
     * 
     * @param dataStored    
     */
    public StoreData(DataStored dataStored){
        this.dbconn = DatabaseConnection.getInstance();
        this.receiveTasks = new ArrayList<Task>();
        lock = new ReentrantLock();
        waitForData = lock.newCondition();
        this.dataStored = dataStored;
    }
    
    /**
     * Run method will store data from tasks
     */
    @Override
    public void run(){
        while(true){
            // Await data if there are no tasks.
            lock.lock();
            try {
                while (receiveTasks.isEmpty()){
                    System.out.println("Waiting for Tasks to be added");
                    waitForData.await();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(StoreData.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
            lock.lock();
            // Iterate through list of task and store data
            try {
                for (Iterator<Task> i = receiveTasks.iterator(); i.hasNext(); ){
                    Task ta = i.next();  
                    if (ta.size() > 0){
                        ArrayList<ParameterSet> dA = new ArrayList<ParameterSet>();
                        ta.getDataElements(dA, 1);
                        dbconn.storeData(ta.getVIN(), ta.getTaskID(), dA.get(0).parameterName, dA.get(0).parameterValue, dA.get(0).timeStamp);
                        ta.deleteDataElements(1);
                        System.out.println("Data stored");
                    }
                    else {
                        // inform if all data is stored and TaskState is end
                        // end remove task
                        if (ta.getTaskState()==TaskState.END){
                            ta.setDataAdded(null);
                            dataStored.dataStored(ta);
                            i.remove();
                        }
                    }
                }
            }
            finally {
                lock.unlock();
            }
            lock.lock();
            // Check if Tasks do not contain data. if so wait.
            try {
                while(noData()){
                    System.out.println("Waiting for Data");
                    waitForData.await();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(StoreData.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Overrides mehtods from interface this method is not needed for DataStorer
     * @return 
     */
    @Override
    public String getVIN() {
        return "all";
    }

    /**
     * Also override from interface, not needed.
     * 
     * @param taTr  List of Tasks
     */
    @Override
    public void addTransmitTasks(ArrayList<Task> taTr) {
        // Not needed.
    }

    /**
     * This method is used to add a list of tasks to be stored.
     * 
     * @param taRe      ArrayList of Tasks to be stored
     */
    @Override
    public void addReceiveTasks(ArrayList<Task> taRe) {
        lock.lock();
        try {
            for (Task ta:taRe){
                ta.setDataAdded(this);
            }
            receiveTasks.addAll(taRe);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add a new Task to be stored to the database
     * 
     * @param ta    Task ot be stored.
     */
    @Override
    public void newTaskToStore(Task ta) {
        lock.lock();
        try {
            ta.setDataAdded(this);
            receiveTasks.add(ta);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Method evaluates whether there is data to store available
     * @return      True of no data is not available
     */
    private boolean noData(){
        boolean noData = true;
        lock.lock();
        try {
            for (Task ta:receiveTasks){
                noData = noData && (ta.size()==0) && (ta.getTaskState() == TaskState.TRANSMIT);
            }
        } finally {
            lock.unlock();
        }
        return noData;
    }

    /**
     * Method being called if data has been added. used to signal, that data 
     * can be stored again.
     */
    @Override
    public void dataAdded() {
        lock.lock();
        try {
            waitForData.signal();
        } finally {
            lock.unlock();
        }
    }
}

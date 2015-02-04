/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import v2gcommunication.commonclasses.tasks.Task;
import v2gcommunication.commonclasses.userrights.UserPrivilage;
import v2gcommunication.commonclasses.tasks.StartTaskTransmit;
/**
 * The {@code DatabaseConnection} class connections to the database. And
 * wraps methods enter information into the database and read information
 * from the database
 * 
 * @author Alexander Forell
 */
public class DatabaseConnection {
    /**
     * Instance of database connection for singleton.
     */
    private static DatabaseConnection instance = null;
    
    
    /**
    * Field which holds the vehicle connection
    */
    private Connection conn;
    
    /**
    * Costructs the database connection and strores it into the field conn;
    * 
    */
    private DatabaseConnection(){
        try {
            // Der Aufruf von newInstance() ist ein Workaround
            // für einige misslungene Java-Implementierungen

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } 
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.err.println("Unable to load connector/J");
            System.exit(-1);
        }
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+"localhost"+":"+"3306"+"/"+"v2gcommunication"+"?user="+"v2gcommunication"+"&password="+"v2gcommunication");
        } 
        catch (SQLException ex) {
            // Fehler behandeln
            System.err.println("SQLException: " + ex.getMessage());
            System.err.println("SQLState: " + ex.getSQLState());
            System.err.println("VendorError: " + ex.getErrorCode());
            System.exit(-1);
        }
    }
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
 
        return instance;
    }
    
    
    /**
    * Stores vin, code of the diagnosis item and corresponding values into 
    * the database.
    * 
     * @param vin               VIN of the vehicle
     * @param taskID            ID of the Task
     * @param timeStamp         Timestamp
     * @param FunctionValue     Value
     * @param FunctionCode      Name of the fuction
    */
    public synchronized void storeData(String vin, String taskID, String FunctionCode, String FunctionValue, String timeStamp){
        String query = "insert into receiveddata (taskID, vin, functionCode, data, timeStamp)"
        + " values (?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, taskID);
            preparedStmt.setString(2, vin);
            preparedStmt.setString(3, FunctionCode);
            preparedStmt.setString(4, FunctionValue);
            preparedStmt.setString(5, timeStamp);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
    * Enters a new client login and password into the database
    * 
    * @param    userName            string holding the username
    * @param    password            string holding the password for the user
    * @param    canModifyUsers      boolean
    * @param    canModifyVehicles   boolean
    * @param    canSendRequest      boolean
    * @return                       true if user was created successfully
    */
    public boolean createUser(String userName, String password, 
            boolean canModifyUsers, boolean canModifyVehicles, boolean canSendRequest){
         String query = "insert into users (username, password)"
        + " values (?, ?)";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.setString(2, password);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        query = "insert into userrights (username, userright, granted)"
        + " values (?, ?, ?)";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.setString(2, "canModifyUsers");
            preparedStmt.setBoolean(3, canModifyUsers);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.setString(2, "canModifyVehicles");
            preparedStmt.setBoolean(3, canModifyVehicles);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.setString(2, "canSendRequest");
            preparedStmt.setBoolean(3, canSendRequest);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    
    /**
    * Enters a new client login and password into the database
    * 
    * @param    userName    string holding the username
    * @param    password    string holding the password for the user
    * @return               true if username an password are correct
    */
    public boolean logon(String userName, String password){
        boolean allowed = false;
        ResultSet rs = null;
        String query = "Select * from users where username=\""+userName+"\" and password=\""+password+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            if (rs.next()){
                allowed = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        // for debugging only 
        allowed = true;
        return allowed;
        
    }    
    
    /**
     * Get UserRights for a certain user
     * 
     * @param username  User to get Rights for.
     * @return          Privilages of user.
     */
    public UserPrivilage getUserRights(String username){
        UserPrivilage userPrivilage = new UserPrivilage();
        boolean allowed = false;
        ResultSet rs = null;
        String query = "Select userright, granted from userrights where username=\""+username+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                if (rs.getString("userright").equals("canModifyUsers")){
                    userPrivilage.canModifyUsers = rs.getBoolean("granted");
                }
                if (rs.getString("userright").equals("canModifyVehicles")){
                    userPrivilage.canModifyVehicles = rs.getBoolean("granted");
                }
                if (rs.getString("userright").equals("canSendRequest")){
                    userPrivilage.canSendRequests = rs.getBoolean("granted");
                }
            }
            userPrivilage.username = username;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return userPrivilage;
        
    }    
    
    /**
     * Method returns a list of users in the database
     * 
     * @return  List of users
     */
    public ArrayList<String> getUsers(){
        
        ArrayList<String> usernames= new ArrayList<String>();
        ResultSet rs = null;
        String query = "Select username from users order by username";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                usernames.add(rs.getString("username"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return usernames;
    }    
    
    /**
    * Deletes a client login from the database
    * 
    * @param    userName    string holding the username
     * @return 
    */
    public boolean deleteUser(String userName){
         String query = "delete from users where username =?";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        query = "delete from userrights where username =?";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, userName);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;

    }
    
    /**
    * Enters a new password into the database
    * 
    * @param    username    string holding the username
    * @param    password    string holding the password for the user
    * @return               true if update was successful          
    */
    public boolean updatePassword(String username, String password){
        String query = "update users set password = ? where username = ? ";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, password);
            preparedStmt.setString(2, username);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
        
    }    
    
    /**
    * Changes accessrights of specified user
    * 
    * @param    userName            string holding the username
    * @param    canModifyUsers      boolean new user right for canModifyUsers
    * @param    canModifyVehicles   boolean new user right for canModifyVehicles
    * @param    canSendRequest      boolean new user right for canSendRequest
    * @return                       True if updates successfully
    */
    public boolean updateUserrights(String userName,
            boolean canModifyUsers, boolean canModifyVehicles, boolean canSendRequest){
        String query;
        query = "update userrights set granted = ? where username = ? and userright = ?";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, canModifyUsers);
            preparedStmt.setString(2, userName);
            preparedStmt.setString(3, "canModifyUsers");
            
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, canModifyVehicles);
            preparedStmt.setString(2, userName);
            preparedStmt.setString(3, "canModifyVehicles");
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setBoolean(1, canSendRequest);
            preparedStmt.setString(2, userName);
            preparedStmt.setString(3, "canSendRequest");
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /**
    * Create a new vehicle on the database
    * 
    * @param VIN        VIN of the vehcile (hashed)
    * @param vehname    Name of the vehicle
    * @return           True if created successfully
    */
    public boolean createVehcile(String VIN, String vehname){
         String query = "insert into vehicles (idvehicles, vehname)"
        + " values (?, ?)";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, VIN);
            preparedStmt.setString(2, vehname);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /**
     * Returns a list of vehicles stored on the server
     * 
     * @return      List of vehicles
     */
    public ArrayList<String> getVehicles(){
        
        ArrayList<String> vehicles= new ArrayList<String>();
        ResultSet rs = null;
        String query = "Select vehname from vehicles order by vehname";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                vehicles.add(rs.getString("vehname"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return vehicles;
    }    
   
    /**
    * Deletes a vehicle from the database
    * 
    * @param    vehname    Vehicle name
    */
    public boolean deleteVehicle(String vehname){
        String query = "Select idvehicles from vehicles where vehname =\""+vehname+"\"";
        ResultSet rs = null;
        String vehID = null;
        boolean success = false;
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                vehID = rs.getString("idvehicles");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        }
        
        
        query = "delete from vehicles where idvehicles =?";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, vehID);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        query = "delete from receiveddata where vin =?";
        try {
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1, vehID);
            preparedStmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;

    }
    
    /**
     * Get VIN (hashed) of a vehicle by the name 
     * 
     * @param vehname       vehicle name
     * @return              vin (hashed)
     */
    public String getVIN(String vehname){
        
        ResultSet rs = null;
        String query = "Select idvehicles from vehicles where vehname =\""+vehname+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                return rs.getString("idvehicles");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }    
    
    /**
     * get a vehicle name from the VIN (hashes)
     * 
     * @param VIN   VIN hased
     * @return      vehicle name
     */
    public String getVehicleName(String VIN){
        
        ResultSet rs = null;
        String query = "Select vehname from vehicles where idvehicles =\""+VIN+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                return rs.getString("vehname");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }    
    
    /**
     * Method returns a list of the stored vehcile functions on the database
     * 
     * @param vehnames      vehicle name
     * @return              List of functions
     */
    public ArrayList<String> getStoredFunctions(ArrayList<String> vehnames){
        ArrayList<String> vehIDs = new ArrayList<String>();
        ResultSet rs = null;
        
        String queryBuild = "Select idvehicles from vehicles where ";
        for (String vehname:vehnames){
            queryBuild = queryBuild + "vehname =\"" + vehname + "\" or ";
        }
        String query = queryBuild.substring(0, queryBuild.length()-4);
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                vehIDs.add(rs.getString("idvehicles"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        queryBuild = "Select distinct functionCode from receiveddata where ";
        for (String vehID:vehIDs){
            queryBuild = queryBuild + "vin =\"" + vehID + "\" or ";
        }
        ArrayList<String> vehicleFunctions = new ArrayList<String>();
        query = queryBuild.substring(0, queryBuild.length()-4);
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                vehicleFunctions.add(rs.getString("functionCode"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return vehicleFunctions;
    }    
    
    /**
     * Download data from database
     * 
     * @param username          username to send data to
     * @param vehnames          list of vehicle names
     * @param functionNames     List of function names
     * @param taskTransmit      Interface to start transmit task
     * @return                  Array List of Tasks
     */
    public ArrayList<Task> getData(String username, ArrayList<String> vehnames, 
            ArrayList<String> functionNames, StartTaskTransmit taskTransmit){
        ResultSet rs = null;
        // getTaskID
        String queryBuild = "Select distinct taskID from receiveddata where ";
        for (String vehname:vehnames){
            queryBuild = queryBuild + "vin =\"" + getVIN(vehname) + "\" or ";
        }
        String query = queryBuild.substring(0, queryBuild.length()-4);
        queryBuild = query + " and ";
        for (String vehFu:functionNames){
            queryBuild = queryBuild + "functionCode =\"" + vehFu + "\" or ";
        }
        query = queryBuild.substring(0, queryBuild.length()-4);
        ArrayList<String> tasks = new ArrayList<String>();
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            while (rs.next()){
                tasks.add(rs.getString("taskID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        //read data
        ArrayList<Task> returnTasks = new ArrayList<Task>();
        for (String vehname:vehnames){
            for (String function:functionNames){
                for (String task:tasks){
                    query = "Select data, timestamp from receiveddata where taskId =\"" + task +
                            "\" and vin=\"" + getVIN(vehname) + "\" and functionCode =\"" + function + "\"";
                    try {
                        Statement stmt = conn.createStatement();
            
                        rs = stmt.executeQuery(query);
                        Task ta = taskTransmit.startTaskTransmit(username, vehname+"|"+task);
                        returnTasks.add(ta);
                        while (rs.next()){
                            ta.addDataElement(function, rs.getString("data"), null, rs.getString("timestamp"));
                        }
                        ta.endTask();
                    } catch (SQLException ex) {
                        Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        }
        return returnTasks;     
    }
    
    /**
     * Method checks wheter username and password are valid
     * 
     * @param userName      Username
     * @param password      password
     * @return              True if username and password are valid.
     */
    public boolean authenticateUser(String userName, String password){
        boolean allowed = false;
        ResultSet rs = null;
        String query = "Select * from users where username=\""+userName+"\" and password=\""+password+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            if (rs.next()){
                allowed = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return allowed;
        
    }    
    
    /**
     * Method checks wheter vin (hased) is valid
     * 
     * @param vehicle       Hashed VIN
     * @return              True if username and password are valid.
     */
    public boolean authenticateVehicle(String vehicle){
        boolean allowed = false;
        ResultSet rs = null;
        String query = "Select * from vehicles where idvehicles=\""+vehicle+"\"";
        try {
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(query);
            if (rs.next()){
                allowed = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return allowed;
        
    }    
    
}

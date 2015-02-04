/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *
 */
package v2gcommunicationserver;

import java.io.*;

/**
 * The {@code Configuration} class wraps fields to configure the server. 
 * 
 * the class constructor loads parameters for the server configuration, such as
 * the port listening for incoming vehicle connection, the port for incoming
 * user connections, and the parameters to connect to the database
 * 
 * @author Alexander Forell
 * 
 */
public class Configuration {
    /**
    * Field which holds the port for incoming vehicle connections
    */
    private int portVehicleCommunication;
    
    /**
    * Field which holds the port for incoming user connections
    */
    private int portUserCommunication;
    
    /**
    * Field which holds the IP-Adress of the database server
    */
    private String databaseServer;
    
    /**
    * Field which holds listening port of the database server
    */
    private int databasePort;
    
    /**
    * Field which holds database scheme
    */
    private String databaseScheme;
    
    /**
    * Field which holds database username
    */
    private String databaseUserName;
    
    /**
    * Field which holds password of the database user
    */
    private String databasePassword;
    /**
     * Constructor reads file ServerConfig.txt and tries stores the fields
     * <p>{@code portVehicleCommunication}
     * <p>{@code portUserCommunication}
     * <p>{@code databaseServer}
     * <p>{@code databasePort}
     * <p>{@code databaseScheme}
     * <p>{@code databaseUserName}
     * <p>{@code databasePassword}
     * 
     * @author Alexander Forell
     */
    Configuration(){
        int i = 1;
        try { 
            BufferedReader  br = new BufferedReader(new FileReader("build/classes/v2gcommunicationserver/ServerConfig.txt"));
            String line = br.readLine();
            while (null!=line) {
                String Param[] = line.split(":");
                if ("PortVehicleCommunication".equals(Param[0])){
                    portVehicleCommunication = Integer.parseInt(Param[1]);
                }
                if ("PortUserCommunication".equals(Param[0])){
                    portUserCommunication = Integer.parseInt(Param[1]);
                }
                if ("databaseServer".equals(Param[0])){
                    databaseServer = Param[1];
                }
                if ("databasePort".equals(Param[0])){
                    databasePort = Integer.parseInt(Param[1]);
                }
                if ("databaseScheme".equals(Param[0])){
                    databaseScheme = Param[1];
                }
                if ("databaseUserName".equals(Param[0])){
                    databaseUserName = Param[1];
                }
                if ("databasePassWord".equals(Param[0])){
                    databasePassword = Param[1];
                }
                i=i+1;
                line = br.readLine();
            }
            br.close();
            
        }
        catch (IOException  e){
            System.err.println("File ServerConfig.txt not found");
        }
        catch ( NumberFormatException e) {
            System.err.println("Wrong number format in line " + i);
        }
    }
    /**
     * Returns an {@code Integer} containing the port opened for incoming 
     * vehicle connections
     * 
     * @return vehicle communication port 
     */
    int getPortVehicleCommunication(){
        return portVehicleCommunication; 
    }
    /**
     * Returns an {@code Integer} containing the port opened for incoming 
     * user connections
     * 
     * @return user communication port 
     */
    int getPortUserCommunication(){
        return portUserCommunication; 
    }
    
    /**
     * Returns a {@code String} containing the IP-adress of the database server
     * 
     * @return IP-adress of database server
     */
    String databaseServer(){
        return databaseServer; 
    }
    
    /**
     * Returns an {@code Integer} containing the port the database server is
     * listening for incoming connections
     * 
     * @return database connection port
     */
    int databasePort(){
        return databasePort; 
    }
    
    /**
     * Returns a {@code String} containing the name of the database scheme 
     * used for the v2gcommunication server
     * 
     * @return name of database scheme
     */
    String databaseScheme(){
        return databaseScheme; 
    }
    
    /**
     * Returns a {@code String} containing the username to logon to the 
     * database server
     * 
     * @return database username
     */
    String databaseUserName(){
        return databaseUserName; 
    }
    
    /**
     * Returns a {@code String} containing the password of the database user
     * 
     * @return database user password
     */
    String databasePassword(){
        return databasePassword; 
    }
}

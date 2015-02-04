/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package v2gcommunicationserver;

import v2gcommunication.commonclasses.sessions.AuthenticateAccess;
import v2gcommunication.commonclasses.sessions.SessionType;

/**
 * Class to authenticate user connecting to the server implements 
 * AuthenticateAccess.
 * 
 * @author Alexander Forell
 */
public class Authenticate implements AuthenticateAccess{

    /**
     * Method runs Authentification for user or vehicle.
     * 
     * @param VIN       VIN or username
     * @param password  password of the user
     * @return          Type of the Session
     */
    @Override
    public SessionType authenticateAccess(String VIN, String password) {
        DatabaseConnection dbconn = DatabaseConnection.getInstance();
        if (dbconn.authenticateUser(VIN, password)) return SessionType.CLIENT;
        if (dbconn.authenticateVehicle(VIN)) return SessionType.VEHICLE;
        return SessionType.UNKNOWN;
    }
    
}

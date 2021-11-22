import java.time.*;
import java.rmi.*;
import java.io.*; 
import java.rmi.server.*;

public class ClockServer extends UnicastRemoteObject implements ClockInterface{
    
    Clock clientClock; // Clock object that is used by the server to return the time to its clients.
    
    public ClockServer() throws RemoteException{
        clientClock = Clock.systemUTC(); // initializing to UTC system time

        try{ //creating clock server binded to path "ClockServer"
            String objPath = "ClockServer"; //name of server location
            Naming.bind(objPath, this); //binding to name on rmiregistry
            System.out.println("Remote Server is running now."); 
        }
        catch(Exception e){
            System.out.println("Exception" + e);
        }
    }
    
    // returns servers time in milliseconds
    public long getTime() throws RemoteException{
        return clientClock.instant().toEpochMilli();
    }

    //return a pair of timed Values, when the message was received and when it was sent (time in milliseconds since January 1, 1970)
    public ClockMessage getTaggedTime() throws RemoteException{
        System.out.println("Request recieved for tagged time");
        long in = clientClock.instant().toEpochMilli();
        long time = clientClock.instant().toEpochMilli();
        System.out.println("In time - " + in + " Clock Server Time - " + time);
        return new ClockMessage(in,time,clientClock.instant().toEpochMilli());
    }

        // creates and starts running the remote clock server
    public static void main(String args[]) throws RemoteException{
        ClockServer clockServer = new ClockServer();
    }
}
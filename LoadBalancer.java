import java.rmi.*;
import java.rmi.server.*;

public class LoadBalancer extends UnicastRemoteObject implements LoadBalancerInterface{

    int noOfServers;
    int noOfRequests;

    public LoadBalancer() throws RemoteException{
        noOfServers = 4;
        noOfRequests = 0;
    }

    public MyInterface getServer() throws RemoteException{
        int serverNo = noOfRequests % noOfServers;
        noOfRequests++;
        MyInterface server = null;
        // return "server"+serverNo;
        try{
            String path = "server" + serverNo;
            System.out.println("Redirecting request to server " +serverNo);
            server = (MyInterface) Naming.lookup(path);
        }
        catch(Exception e){
            System.out.println("Unable to connect to server, trying next one " + e);
            server = this.getServer();
        }
        return server;
    }

    public static void main(String args[]) throws RemoteException{
        LoadBalancer token = new LoadBalancer();
        try{
            String objPath = "LoadBalancer"; //name of server location
            // TokenInterface stub = (TokenInterface) UnicastRemoteObject.exportObject(token,0);
            Naming.bind(objPath, token); //binding to name on rmiregistry
            System.out.println("Remote Server is running now."); 
        }
        catch(Exception e){
            System.out.println("Exception" + e);
        }
    }
}
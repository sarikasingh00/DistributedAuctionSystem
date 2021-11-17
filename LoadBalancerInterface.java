import java.rmi.*;
public interface LoadBalancerInterface extends Remote {

    public MyInterface getServer() throws RemoteException;
}
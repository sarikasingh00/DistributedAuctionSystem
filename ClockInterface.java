import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.*;

public interface ClockInterface extends Remote{
    // return this clock’s estimate of the time in milliseconds since January 1, 1970
    public long getTime() throws RemoteException;
    
    // return a pair of timed Values, when the message was received and when it was sent (time in milliseconds since January 1, 1970)
    public ClockMessage getTaggedTime() throws RemoteException;
}
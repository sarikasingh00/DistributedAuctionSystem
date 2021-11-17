import java.rmi.*;
import java.util.Date;  
public interface MyInterface extends Remote {

	public boolean bid(float price, int client_no, int item_id) throws RemoteException;  
	public void sendRequest() throws RemoteException;
	public void critical_section(float bid, int client_no, long bid_time, int item_id) throws RemoteException;
	public void recieveRequest(int i, int n) throws RemoteException;
	public void releaseToken() throws RemoteException;
	public String getName() throws RemoteException;
	public void addNewItem(int item_id, String item_name, Date date, int  duration) throws RemoteException;
	// public float get_max_bid() throws RemoteException;
	// public boolean auctionOn(long curr_time) throws RemoteException;
	public float[] get_winner(int item_id) throws RemoteException;
}
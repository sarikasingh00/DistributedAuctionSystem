import java.rmi.*;
import java.rmi.server.*;
import java.time.*;
import java.util.*;
import java.io.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class RMIServer extends UnicastRemoteObject implements MyInterface
{   
    // float max_bid;
    int serverNo;
    Clock clock;
    int RN[];
    boolean critical;
    // int token[];
    // int queue[];
    int no_of_requests;
    TokenInterface token;
    // int number_of_bids;
    // long startTime;
    // long endTime;

    public RMIServer(int serverNo) throws RemoteException{ 
        System.out.println("Remote Server is running now."); 
        // max_bid = 0;
        this.serverNo = serverNo;
        this.clock = Clock.systemUTC();
        RN = new int[3];
        no_of_requests = 0;
        critical = false;

        try{
            token = (TokenInterface) Naming.lookup("Token");
        }
        catch(Exception e) { 
            System.out.println("Exception occurred : "+e.getMessage());
        }

        // number_of_bids = 0;
        // DefaultSystemTime time = new DefaultSystemTime();

        // try{
        //     String objPath = "";
        //     SystemTime stub = (SystemTime) UnicastRemoteObject.exportObject(time,0);
        //     Naming.bind(objPath, time);
        // }
        // catch(Exception e){
        //     System.out.println("System time registry existsm skipping making new one");
        // }
        
        // startTime = time.getSystemTime();
        // endTime = startTime + 30000;

    }
    
    public static void main(String arg[]){ 
        // String objPath = "//localhost:1099/SystemTime";
        int serverNo = Integer.parseInt(arg[0]);
        try{ 
                RMIServer p=new RMIServer(serverNo);
                Naming.rebind("server"+serverNo,p);

                // DefaultSystemTime time = new DefaultSystemTime();
                // SystemTime stub = (SystemTime) UnicastRemoteObject.exportObject(time,0);
                // Naming.bind(objPath, time);
            }  
            catch(Exception e){ 
                System.out.println("Exception occurred : "+e.getMessage()); 
            } 
        }

        @Override
        public boolean bid(float bid, int client_no, int item_id) throws RemoteException{

            // DefaultSystemTime time = new DefaultSystemTime();
            try{
            ClockInterface cs = (ClockInterface) Naming.lookup("ClockServer");
            ClockMessage taggedTime = cs.getTaggedTime();

            long start = taggedTime.in;
            long end = taggedTime.out;
            long serverTime = taggedTime.time;

            long old_time = clock.instant().toEpochMilli();

            long rtt = (end-start)/2; //calculate round trip time
            long updatedTime = serverTime + rtt; // new Local Clock Time
            long diff = updatedTime - old_time; // the difference between old and new time
            Duration duration = Duration.ofMillis(updatedTime - clock.instant().toEpochMilli());

            clock = clock.offset(clock, duration);

            long bid_time = updatedTime;

            System.out.println("Received bid from client " +client_no + ". Bid =" + bid + "  at server at time " + bid_time + "for item ID - " + item_id);
            // number_of_bids++;
            // if (auctionOn(bid_time)){
            //     if(bid>max_bid){
            //         // number_of_bids++;
            //         max_bid = bid;
            //         this.client_no = client_no;
            //     }
            // }
            // else{
            //     System.out.println("Sold at " + this.get_max_bid());
            //     return false;
            // }
            // return true;
            
            if(token.getOwner() == -1){
                token.setOwner(serverNo);
                // System.out.println("No owner");
                no_of_requests++;
                RN[serverNo]++;
            }
            else{
                sendRequest();
            }
            while(token.getOwner()!=serverNo);
            System.out.println("Lock Acquired");
            critical = true;
            critical_section(bid, client_no, bid_time, item_id);
            critical = false;
            releaseToken();
            System.out.println("Lock Released\n");

        }
        catch(Exception e){
            System.out.println(e);
        }


        //recieve bid
        //send token request (send_request(server_no, n+1))to all servers via RMI url
        //wait for token - while(token.owner !=serverNo) wait else enter cr
        //if token granted, critical_section()
        //complete CR, release() and send to next in queue, else wait.

        //recieve_request -> add to queuue 
        return true;     
    }

    public void sendRequest() throws RemoteException{
        no_of_requests++;
        for(int i =0;i<3;i++){
            try{
                MyInterface server = (MyInterface) Naming.lookup("server" + i);
                server.recieveRequest(serverNo, no_of_requests);
            }
            catch(Exception e){ 
                System.out.println("Exception occurred : " + e.getMessage()); 
            } 
        }
    }

    public void critical_section(float bid, int client_no, long bid_time, int item_id) throws RemoteException{
        // System.out.println("hello " + bid);
        float max_bid = 0;
        float id = 0;
        File file2 = null;
        Scanner sc2 = null; 
        try{
            File file1 = new File("bid" +item_id+".txt");
            if (file1.createNewFile()) {  
                // System.out.println("New File is created!");  
                BufferedWriter outputWriter = new BufferedWriter(new FileWriter("bid" +item_id+".txt", true));
                outputWriter.write("0,0,0,0");//appends the string to the file
                outputWriter.flush();  
                outputWriter.close(); 
            } else {  
                // System.out.println("File already exists.");  
            }  

            Scanner sc1 = new Scanner(file1);

            file2 = new File("bid" +item_id+"_copy.txt");
            if (file2.createNewFile()) {  
                // System.out.println("New File is created!");  
                BufferedWriter outputWriter = new BufferedWriter(new FileWriter("bid" +item_id+"_copy.txt", true));
                outputWriter.write("0,0,0,0");//appends the string to the file
                outputWriter.flush();  
                outputWriter.close();
            } else {  
                // System.out.println("File already exists.");  
            }  
            sc2 = new Scanner(file2);

            sc1.useDelimiter(","); 
            id  = Float.parseFloat(sc1.next());
            max_bid  = Float.parseFloat(sc1.next());
    
            System.out.println("Previous maximum bid - " + max_bid);

            
        }
        catch(Exception e){
            System.out.println(e);
            System.out.println("Could not access file bid1.txt, trying to access backup");
            sc2.useDelimiter(","); 
            id  = Float.parseFloat(sc2.next());
            max_bid  = Float.parseFloat(sc2.next());
            System.out.println("Previous maximum bid - " + max_bid);
        }
        if(max_bid<bid){
                try{

                    BufferedWriter outputWriter = new BufferedWriter(new FileWriter("bid" +item_id+".txt"));
                        
                    outputWriter.write(item_id+","+bid + "," + client_no +"," + bid_time);
                        
                    outputWriter.flush();  
                    outputWriter.close();

                    outputWriter = new BufferedWriter(new FileWriter("bid" +item_id+"_copy.txt"));
                        
                    outputWriter.write(item_id+","+bid + "," + client_no +"," + bid_time);
                        
                    outputWriter.flush();  
                    outputWriter.close();
                }
                catch (Exception e){
                    System.out.println("Error writing to bid files");

                }
        }

        try{
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter("transactions_server" + this.serverNo + ".txt", true));
                    
            outputWriter.write(item_id + ","+bid + "," + client_no +"," + bid_time +"\n");
                
            outputWriter.flush();  
            outputWriter.close();
        }
        catch(Exception e){
            System.out.println("Failed to write to transaction file " + e);
        }
    }

    public void recieveRequest(int i, int n) throws RemoteException{
        // System.out.println("Recieved request from " + i + "n-" + n + "RN[i]" + RN[i] + "token owner = " + token.getOwner());
        // System.out.println("Recieved request from " + i);
        if(RN[i] <=n){
            RN[i] = n;
            // System.out.println("n-" + n + "new RN[i]" + RN[i] + "token[i] " + token.getToken()[i]);
            if (token.getToken()[i] + 1 == RN[i]){
                if(token.getOwner() == serverNo){
                    if(critical){
                    // token.queue = i;
                        // System.out.println("Add to queue");
                        token.getQueue()[token.getTail()] = i;
                        token.setTail(token.getTail()+1);
                    }
                    else{
                        // System.out.println("Queue empty, setting owner");
                        token.setOwner(i);
                    }
                }
            }
        }
    }
    
    public void releaseToken() throws RemoteException{
        // System.out.println("old token[i] " + token.getToken()[serverNo] + "RN[i] " + RN[serverNo]);
        token.setToken(serverNo,  RN[serverNo]);
        // System.out.println("new token[i] " + token.getToken()[serverNo]);
        if(token.getHead() != token.getTail()){
            // System.out.println("Release token");
            token.setOwner(token.getQueue()[token.getHead()]);
            // System.out.println("New owner" + token.getOwner());
            token.setHead(token.getHead()+1);
        }
        // else{
            // token.setOwner(-1);
        // }
    }

    public String getName() throws RemoteException{
        return "server" + serverNo;
    }

    public void addNewItem(int item_id, String item_name, Date date, int  duration) throws RemoteException{
        try
        {
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter("items.txt", true));
                    
            // outputWriter.write(bid + "," + client_no +"," + bid_time + "\n");
            outputWriter.write(item_id + "," + item_name + ","+ date + ","+duration);//appends the string to the file
                
            outputWriter.flush();  
            outputWriter.close();

            // String filename= "items.txt";
            // FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            
            // fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    // public static void main(String args[]){ 
    //     // String objPath = "//localhost:1099/SystemTime";
    //     try{ 
            
    //         int server_no =  Integer.parseInt(args[0]);
    //         RMIServer p=new RMIServer(server_no);
    //         Naming.rebind("server" + server_no, p);
    //     }  
    //     catch(Exception e){ 
    //         System.out.println("Exception occurred : "+e.getMessage()); 
    //     } 
    // }

    
    // @Override
    // public float get_max_bid() throws RemoteException {
    //     return max_bid;
    // }
    // @Override
    // public boolean auctionOn(long curr_time) throws RemoteException {
    //     if(curr_time <= endTime){
    //         return true;
    //     }
    //     return false;
    // }
    // @Override
    public float[] get_winner(int item_id) throws RemoteException {
        float return_vals[] = new float[2];
        try{
            File file1 =new File("bid" +item_id+".txt");
            Scanner sc = new Scanner(file1);
            sc.useDelimiter(","); 
            float id  = Float.parseFloat(sc.next());
            float max_bid  = Float.parseFloat(sc.next());
            float client_no  = Float.parseFloat(sc.next());

            return_vals[0] = client_no;
            return_vals[1] = max_bid;
            // System.out.println("Previous maximum bid - " + max_bid);
        }
        catch(Exception e){
            System.out.println(e);
        }
        return return_vals;
    }
}

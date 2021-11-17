import java.rmi.*;
import java.io.*; 
import java.time.*;
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.util.*;  
// import java.time.LocalDateTime;    
public class RMIClient
{   
    public static void  main(String args[])
    { 
        try{
            int client_no =  Integer.parseInt(args[0]);
            Clock clientClock = Clock.systemUTC();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
            LoadBalancerInterface lb = (LoadBalancerInterface) Naming.lookup("LoadBalancer");

            System.out.println("1. Admin Login\n2. Bidder Login\nEnter choice");
            int ch = Integer.parseInt(br.readLine());
            if(ch==1){
                while(true){
                    System.out.println("Enter username for admin-");
                    String username = br.readLine();
                    System.out.println("Enter password for admin-");
                    String password = br.readLine();

                    if(username.equals("admin") && password.equals("admin123")){
                        System.out.println("Login successful");
                        break;
                    }
                    else{
                        System.out.println("Incorrect username or password");
                    }
                }
                System.out.println("Enter auction details for new items -");
                System.out.println("Item ID - ");
                int item_id = Integer.parseInt(br.readLine());
                System.out.println("Item Name - ");
                String item_name=br.readLine();
                System.out.println("Auction date - dd/MM/yyyy");
                String auction_date=br.readLine();
                System.out.println("Start time - h:mm a");
                String auction_time=br.readLine();
                Date date=new SimpleDateFormat("dd/MM/yyyy h:mm a").parse(auction_date + " " + auction_time);  
                System.out.println(date);
                System.out.println("Duration (in seconds)- ");
                int duration = Integer.parseInt(br.readLine());

                try{

                    MyInterface server = lb.getServer();
                    System.out.println("Connecting to " + server.getName());
                    server.addNewItem(item_id, item_name, date, duration);
                }
                catch(Exception e){
                    System.out.println(e);
                }
            }
            else{
                // System.out.println("Client side -");
                System.out.println("1. Ongoing Auctions\n2. Future Auctions\nEnter choice");
                int auction_ch = Integer.parseInt(br.readLine());
                File file = new File("items.txt");
                Scanner sc = new Scanner(file);
                // sc1.useDelimiter(",");
                if(auction_ch==1){
                    System.out.println("Active Auctions -");
                }
                else{
                    System.out.println("Future Auctions -");
                }
                Date endTime = new Date();
                ArrayList<Date> endTimeList = new ArrayList<Date>(5);
                while(sc.hasNext()){
                    String line = sc.nextLine();
                    String item[] = line.split(",");
                    String item_id = item[0];
                    String name = item[1];
                    // System.out.println(item[2]);
                    Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(item[2]);
                    int duration = Integer.parseInt(item[3]);
                    // LocalDateTime now = LocalDateTime.now();
                    endTime = new Date(date.getTime() + duration*1000);
                    endTimeList.add(endTime);
                    // System.out.println(endTime);
                    Date now = new Date();
                    if(auction_ch ==1){
                        if (date.before(now) && endTime.after(now)){
                            System.out.println("\nItem name - " + name);
                            System.out.println("Item ID - " + item_id);
                            System.out.println("Auction Start - " + date);
                            System.out.println("Auction Duration - " + duration + " seconds");
                        }
                    }
                    else if(auction_ch == 2){
                        if (date.after(now)){
                            System.out.println("\nItem name - " + name);
                            System.out.println("Item ID - " + item_id);
                            System.out.println("Auction Start - " + date);
                            System.out.println("Auction Duration - " + duration + " seconds");
                        }
                    }
                }
                if(auction_ch==1){
                    System.out.println("\nEnter item ID to bid for -");
                    int item_ch = Integer.parseInt(br.readLine());
                    boolean flag = true;
                    Date now = new Date();
                    endTime = endTimeList.get(item_ch);
                    // System.out.println(now);
                    // System.out.println(endTime);
                    // System.out.println(endTime.after(now));
                    // System.out.println(now.before(endTime));
                    MyInterface server = null;
                    while(endTime.after(now)){
                        System.out.println("\nEnter bid"); 
                        float input= Float.parseFloat(br.readLine()); 
                        try{
                            server = lb.getServer();
                            System.out.println("Sending request to " + server.getName());
                        }
                        catch(Exception e) { 
                            System.out.println("Exception occurred : "+ e);
                        }
                        // MyInterface server= (MyInterface) Naming.lookup(path);
                        flag = server.bid(input, client_no, item_ch);
                        now = new Date();
                    }
                    
                    server = lb.getServer();
                    System.out.println("Connecting to " + server.getName());
                    float winner_details[] = server.get_winner(item_ch);
                    System.out.println("Winner - Client ID " +winner_details[0]);
                    System.out.println("Sold for - " +winner_details[1]);
                }
            }
        }
        catch(Exception e){
            System.out.println("Exception occurred : "+ e);

        }   
    } 
}
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Node3 {

    public static DatagramSocket client;
    public static int indicator;
    public static int yesCount = 0;
    public static Boolean flag;
    public static String nodeVoted;

    public static void noResponseTime(int n) throws SocketException {
        int k = n * 1000; // converting milliseconds to Seconds
        client.setSoTimeout(k); // 1sec = 1000 milliseconds --> 10 sec
    }

    public static int getResponseTime(DatagramSocket client) throws IOException {
        byte timeBuf[] = new byte[3];
        DatagramPacket crashTime = new DatagramPacket(timeBuf,timeBuf.length);
        client.receive(crashTime);
        String coordinatorData =new String(crashTime.getData());
        String removeExtra = coordinatorData.replaceAll("\\s+"," ").trim();
        int dataInInt = Integer.parseInt(removeExtra);
        return dataInInt;
    }

    public static void crashTime(int n) throws InterruptedException {
        int k = n * 1000; // converting milliseconds to Seconds
        Thread.sleep(k);
    }

    public static void reciveMsgFromCodinator(DatagramPacket msg) throws IOException {
        try{
            flag = true;
            nodeVoted = "Yes";
            yesCount = yesCount + 1;
            client.receive(msg);
            String coordinatorData2 =new String(msg.getData());
            String removeExtraSpace = coordinatorData2.replaceAll("\\s+"," ").trim();
            System.out.println("Co-ordinator says: "+ removeExtraSpace);
        }
        catch (IOException e){
            flag = false;
            nodeVoted = "No";
            indicator = 4;
            System.err.println("Co-ordinator haven't responded with in the setOut time limit");
            System.out.println("So globaly Aborting the Commit");
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        client = new DatagramSocket(4163);
        InetAddress add = InetAddress.getByName("localhost");

//      Get the times from the Co-oridnator.
        int setCrashTime = getResponseTime(client);
        int setNoResponseTime = getResponseTime(client);

        System.out.println("The crash and Response Time is "+ setCrashTime + " "+ setNoResponseTime);

        byte buf[] = new byte[7];
        System.out.println("\n\n\n----------Transaction Start----------\n\n\n ");
//      Voting Yes for the Commit Message.
        crashTime(setCrashTime);
        noResponseTime(setNoResponseTime);

        DatagramPacket recevingTheCommitMsg = new DatagramPacket(buf,buf.length);
        reciveMsgFromCodinator(recevingTheCommitMsg);

        if(flag == true){
            byte buf1[] = nodeVoted.getBytes();
            DatagramPacket votedYes = new DatagramPacket(buf1,buf1.length,add,4160);
            client.send(votedYes);
            System.out.println("Voted Yes to Coordinator");
        }
        else if (flag == false){
            byte buf1[] = nodeVoted.getBytes();
            DatagramPacket votedYes = new DatagramPacket(buf1,buf1.length,add,4160);
            client.send(votedYes);
            System.out.println("Voted No to Coordinator");
            System.out.println("\n\n\n--------Transaction Aborted--------\n\n\n ");
            System.exit(0);
        }


//      Receving the Global Commit Msg from Coordinator.
        byte buf2[] = new byte[24];
        DatagramPacket recevingGlobalTheCommitMsg = new DatagramPacket(buf2,buf2.length);
        reciveMsgFromCodinator(recevingGlobalTheCommitMsg);

        if(flag == true){
            byte buf1[] = nodeVoted.getBytes();
            DatagramPacket votedYes = new DatagramPacket(buf1,buf1.length,add,4160);
            client.send(votedYes);
            System.out.println("Voted Yes to Coordinator");
        }
        else{
            byte buf1[] = nodeVoted.getBytes();
            DatagramPacket votedYes = new DatagramPacket(buf1,buf1.length,add,4160);
            client.send(votedYes);
            System.out.println("Voted No to Coordinator");
            System.out.println("\n\n\n--------Transaction Aborted--------\n\n\n ");
            System.exit(0);
        }

        if(yesCount == 2){
            System.out.println("Local Commit is Done Sucessfully");
            System.out.println("\n\n\n-------Transaction Successful-------\n\n\n ");
        }
        else{
            System.out.println("Voted No to Coordinator");
            System.out.println("\n\n\n--------Transaction Aborted--------\n\n\n ");
        }
        client.close();
    }
}

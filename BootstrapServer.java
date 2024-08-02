import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class BootstrapServer {

    public static void main(String args[])
    {
        DatagramSocket sock = null;
        String s;
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        List<Neighbour> joinedNodes = new ArrayList<Neighbour>();

        String[] filenames = {
                "Adventures_of_Tintin",
                "Jack_and_Jill",
                "Glee",
                "The_Vampire Diaries",
                "King_Arthur",
                "Windows_XP",
                "Harry_Potter",
                "Kung_Fu_Panda",
                "Lady_Gaga",
                "Twilight",
                "Windows_8",
                "Mission_Impossible",
                "Turn_Up_The_Music",
                "Super_Mario",
                "American_Pickers",
                "Microsoft_Office_2010",
                "Happy_Feet",
                "Modern_Family",
                "American_Idol",
                "Hacking_for_Dummies"
        };

        try
        {
            sock = new DatagramSocket(55555);

            echo("Bootstrap Server created at 55555. Waiting for incoming data...");

            while(true)
            {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                byte[] data = incoming.getData();
                s = new String(data, 0, incoming.getLength());

                //echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + s);

                StringTokenizer st = new StringTokenizer(s, " ");

                String length = st.nextToken();
                String command = st.nextToken();
                if (command.equals("REG")) {
                    String reply = "REGOK ";

                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();
                    if (nodes.size() == 0) {
                        reply += "0";
                        nodes.add(new Neighbour(ip, port, username));
                    } else {
                        boolean isOkay = true;
                        for (int i=0; i<nodes.size(); i++) {
                            if (nodes.get(i).getPort() == port) {
                                if (nodes.get(i).getUsername().equals(username)) {
                                    reply += "9998";
                                } else {
                                    reply += "9997";
                                }
                                isOkay = false;
                            }
                        }
                        if (isOkay) {
                            if (nodes.size() == 1) {
                                reply += "1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
                            } else if (nodes.size() == 2) {
                                reply += "2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort();
                            } else {
                                Random r = new Random();
                                int Low = 0;
                                int High = nodes.size();
                                int random_1 = r.nextInt(High-Low) + Low;
                                int random_2 = r.nextInt(High-Low) + Low;
                                while (random_1 == random_2) {
                                    random_2 = r.nextInt(High-Low) + Low;
                                }
                                echo (random_1 + " " + random_2);
                                reply += "2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " + nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
                            }
                            nodes.add(new Neighbour(ip, port, username));
                        }
                    }

                    reply = String.format("%04d", reply.length() + 5) + " " + reply + "\\";

                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                    sock.send(dpReply);
                } else if (command.equals("UNREG")) {
                    String ip = st.nextToken();
                    int port = Integer.parseInt(st.nextToken());
                    String username = st.nextToken();
                    for (int i=0; i<nodes.size(); i++) {
                        if (nodes.get(i).getPort() == port) {
                            nodes.remove(i);
                            String reply = "0012 UNROK 0";
                            DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                            sock.send(dpReply);
                        }
                    }
                } else if (command.equals("ECHO")) {
                    for (int i=0; i<nodes.size(); i++) {
                        echo(nodes.get(i).getIp() + " " + nodes.get(i).getPort() + " " + nodes.get(i).getUsername());
                    }
                    String reply = "0012 ECHOK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                    sock.send(dpReply);
                }
                else if (command.equals("PRINTNODE")) {
                    String ip = st.nextToken();
                    System.out.println("----------COMMAND PRINTNODE----------------");
                    System.out.println("PRINTNODE REQUEST IP ADDRESS "+ ip);

                    for (int i=0; i<nodes.size(); i++) {
                        if (nodes.get(i).getIp().equals(ip)) {
                            System.out.println("PRINTNODE NODE FOUND-> NODE NAME " + nodes.get(i).getUsername());
                            String[] files = nodes.get(i).getFilenames();
                            List<Neighbour> neighbours = nodes.get(i).getNodes();
                            for (int no=0; no < neighbours.size(); no++) {
                                Neighbour temp = neighbours.get(no);
                                System.out.println(" PRINTNODE --------- Neighbout NODE:"+ no);
                                System.out.println(" Neighbout NODE IP :"+ temp.getIp());
                                System.out.println(" Neighbout NODE USERNAME :"+ temp.getUsername());

                            }
                            for (String file : files) {
                                echo("Files Shared :" + file);
                            }
                            break;
                        }
                    }
                    String reply = "0012 PRINTNODE 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                    sock.send(dpReply);





                }else if (command.equals("JOIN")) {
                    String ip = st.nextToken();
                    Neighbour joinTempNode = null;     // Temp Node
                    boolean isNodeExisting = false;
                    System.out.println("JOIN IP ADDRESS:"+ip);

                    //Checking node is already registered or Not
                    for (int i=0; i<nodes.size(); i++) {
                        if (nodes.get(i).getIp().equals(ip)) {
                            isNodeExisting = true;
                            joinTempNode = nodes.get(i);
                            break;
                        }
                    }

                    if(isNodeExisting){ // Node validation to check node is already registered or not
                        String[] sharingFiles = getRandomStrings(filenames, 4); // Randomly get 4 files in the list to share
                        String reply = "JOIN OK ";
                        if (joinedNodes.isEmpty()){
                            joinTempNode.setFilenames(sharingFiles);
                            joinedNodes.add(joinTempNode);
                            reply += "NODE JOINED SUCCESSFULLY SINGLE NODE";
                        }else if(joinedNodes.size() == 1) {
                            joinTempNode.setFilenames(sharingFiles);
                            joinTempNode.setNodes(joinedNodes.getFirst());
                            joinedNodes.add(joinTempNode);

                            reply += "SINGLE NODE EXISTING";
                        } else if (joinedNodes.size() == 2) {
                            joinTempNode.setFilenames(sharingFiles);
                            joinTempNode.setNodes(nodes.getFirst());
                            joinTempNode.setNodes(nodes.get(1));
                            joinedNodes.add(joinTempNode);

                            reply += "TWO NODES JOINED";

                        }else {

                            Random r = new Random();
                            int Low = 0;
                            int High = nodes.size();
                            int random_1 = r.nextInt(High - Low) + Low;
                            int random_2 = r.nextInt(High - Low) + Low;
                            while (nodes.get(random_1) == joinTempNode){
                                random_1 = r.nextInt(High-Low) + Low;
                            }
                            while (nodes.get(random_2) == joinTempNode){
                                random_2 = r.nextInt(High-Low) + Low;
                            }
                            while (random_1 == random_2) {
                                random_2 = r.nextInt(High-Low) + Low;
                            }

                            joinTempNode.setFilenames(sharingFiles);
                            joinTempNode.setNodes(nodes.get(random_1));
                            joinTempNode.setNodes(nodes.get(random_2));
                        }


                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                        sock.send(dpReply);

                    }else{
                        echo("Node Not Registered ");
                        String reply = "0012 NODE NOT REG 0";
                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                        sock.send(dpReply);
                    }


                }else if (command.equals("SEARCH")) {
                    String ip = st.nextToken();
                    String searchValue = st.nextToken();
                    Neighbour searchSourceNode = null;     // Temp Node
                    boolean isNodeExisting = false;
                    System.out.println("SEARCH IP ADDRESS:"+ip);

                    //Checking node is already registered or Not
                    for (int i=0; i<joinedNodes.size(); i++) {
                        if (joinedNodes.get(i).getIp().equals(ip)) {
                            isNodeExisting = true;
                            searchSourceNode = nodes.get(i);
                            break;
                        }
                    }

                    if(isNodeExisting){
                       String foundIp =  searchSourceNode.search(searchValue,5);
                        System.out.println("Found File in:"+foundIp);

                    }

                }else{
                    echo("Wrong Command");
                }

            }
        }

        catch(IOException e)
        {
            System.err.println("IOException " + e);
        }
    }



    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }


    public static String[] getRandomStrings(String[] array, int n) {
        // Validate the value of n
        if (n < 3 || n > 4) {
            throw new IllegalArgumentException("The number of strings must be 3 or 4.");
        }

        // Check if the input array has enough elements
        if (array.length < n) {
            throw new IllegalArgumentException("The input array does not have enough elements.");
        }

        // Convert array to a list to use Collections.shuffle
        List<String> list = new ArrayList<>(List.of(array));

        // Shuffle the list to randomize the order
        Collections.shuffle(list);

        // Create a new array with the specified number of elements
        String[] newArray = new String[n];
        for (int i = 0; i < n; i++) {
            newArray[i] = list.get(i);
        }

        return newArray;
    }
}



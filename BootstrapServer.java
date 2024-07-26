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

        String[] filenames = {
                "Adventures of Tintin",
                "Jack and Jill",
                "Glee",
                "The Vampire Diaries",
                "King Arthur",
                "Windows XP",
                "Harry Potter",
                "Kung Fu Panda",
                "Lady Gaga",
                "Twilight",
                "Windows 8",
                "Mission Impossible",
                "Turn Up The Music",
                "Super Mario",
                "American Pickers",
                "Microsoft Office 2010",
                "Happy Feet",
                "Modern Family",
                "American Idol",
                "Hacking for Dummies"
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
                    Neighbour node = null;     // Temp Node
                    boolean isNodeExisting = false;
                    System.out.println("JOIN IP ADDRESS:"+ip);

                    for (int i=0; i<nodes.size(); i++) {
                        if (nodes.get(i).getIp().equals(ip)) {
                            isNodeExisting = true;
                            node = nodes.get(i);
                            break;
                        }
                    }

                    if(isNodeExisting){ // Node validation to check node is already registered or not
                        String[] sharingFiles = getRandomStrings(filenames, 4); // Randomly get 4 files in the list to share
                        Random r = new Random();
                        int Low = 0;
                        int High = nodes.size();
                        int random_1 = r.nextInt(High-Low) + Low;
                        int random_2 = r.nextInt(High-Low) + Low;

                        node.setFilenames(sharingFiles);
                        node.setNodes(nodes.get(random_1));
                        node.setNodes(nodes.get(random_2));

                    }else{
                        echo("Node Not Registered ");
                        String reply = "0012 NODE NOT REG 0";
                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                        sock.send(dpReply);
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



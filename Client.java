import java.net.*;

public class Client {

    public static void main(String[] args) throws Exception {
        // Replace these values with actual client details
        String clientIP = "192.168.1.100";
        int clientPort = 12345;  // Replace with actual port number
        String username = "client1";

        // Create a UDP socket
        DatagramSocket socket = new DatagramSocket();

        // Prepare registration message
        String message = "0028 REG " + clientIP + " " + clientPort + " " + username;
        byte[] sendData = message.getBytes();
//        0028 UNREG 64.12.123.190 432//
        String messageUnREG = "0028 UNREG " + clientIP + " " + clientPort + " " + username;
        byte[] sendDataUnreg = messageUnREG.getBytes();

        // Specify Bootstrap Server address and port
        InetAddress serverAddress = InetAddress.getByName("192.168.8.102");
        int serverPort = 55555;

        // Create DatagramPacket and send the registration message
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
        socket.send(sendPacket);

        System.out.println("Registration request sent to the server.");

        DatagramPacket sendPacketUnReg = new DatagramPacket(sendDataUnreg, sendData.length, serverAddress, serverPort);
//        socket.send(sendPacketUnReg);


        // Optional: Receive response from the server
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);

        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Server response: " + response);

        // Close the socket
        socket.close();
    }
}

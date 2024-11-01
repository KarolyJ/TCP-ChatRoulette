import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {

  public static void main(String[] args) throws IOException {
    try {
      //Creates a stream socket and connects it to the specified prot
      InetAddress host = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);
      Socket socket = new Socket(host, port);
      System.out.println("You're now connected to the server");

      //Create output writer with autoflush enabled
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //handle incomming messages 
            new Thread(() -> {
                try {
                    String incommingMessage;
                    while((incommingMessage = in.readLine()) != null) {
                        System.out.println(incommingMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

      Scanner scan = new Scanner(System.in);
      String inputString;

      while(true) {
        //handle input from client console
        inputString = scan.nextLine();
        if(inputString.equals("exit")) {
          System.out.println("Goodbye!");
          break;
      }
      out.println(inputString);
    }
  } catch (IOException e) {
    e.printStackTrace();
  }
}
}

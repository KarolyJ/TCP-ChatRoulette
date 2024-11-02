import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class server {
    private static final int SERVER_PORT = 4445;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main (String[] args) throws IOException, InterruptedException {
        //socket listening on this port
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("The server is listening on: " + SERVER_PORT);

        try {
        //server runs forever, and it waits for the next client to connect
           while(true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client has connected " + clientSocket);
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            //start thread that handles the client
               Thread handlerThread = new Thread(clientHandler);
               handlerThread.start();
            }
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender, ClientHandler receiver) {
        //the server send the message to every client except to the sender.
        for(ClientHandler client : clients) {
            if(client != sender && client == receiver) {
                client.sendMessage(message);
            }
        }
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }


    private static class ClientHandler implements Runnable{
        private BufferedReader in;
        private PrintWriter out;
        private String Username;
        private Socket clientSocket;
        private ClientHandler pair;
        private boolean isPaired;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            System.out.println(clientSocket.getPort() + " has connected to the server.");
           
            try {
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.Username = getUsername();
                this.isPaired = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 

        @Override
        public void run() {
            try {
                //get the pair for this client
                this.pair = getPairSecond();
                out.println("Welcome to the chat room " + Username);
                out.println("Paired with: " + this.pair.Username);
                out.println("Enter your message: ");
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    //switch partners during the chat
                    if (inputLine.equalsIgnoreCase("switch")) {
                        this.isPaired = false;
                        if (this.pair != null) this.pair.isPaired = false;
                        this.pair = getPairSecond();
                        out.println("Paired with: " + this.pair.Username);
                        //handle when partner exits the chat
                        //remove this from the clients and make the pair look for a new partner
                        //TODO exit doesn't remove this client from the array
                    } else if (inputLine.equalsIgnoreCase("exit")) {
                        if (this.pair != null) this.pair.isPaired = false;
                        this.pair.getPairSecond();
                        this.pair.out.println("A partnerem vele van kapcsolatben még:" + getUsername());
                        clients.remove(this);
                        out.println(clients.size());
                    }
                    ClientHandler receiver = this.getPair();
                    if (receiver != null) {
                        broadcast("[" + Username + "] has written: " + inputLine, this, receiver);
                    } else {
                        out.println("No partner available to receive your message.");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private ClientHandler getPairSecond() {
            synchronized (this) {
                try {
                    while (!this.isPaired) {
                        out.println("Waiting for your partner...");
                        int randomNumber = getRandomNumber(0, clients.size());
                        ClientHandler randomClient = clients.get(randomNumber);

                        if (randomClient != this && !randomClient.isPaired) {
                            // Set the pairing relationship between this client and the chosen random client
                            randomClient.setPair(this);
                            this.pair = randomClient;
                            this.isPaired = true;
                            randomClient.isPaired = true;
                            return randomClient;
                        }

                        // Wait briefly before trying again
                        this.wait(3000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
            return this.pair;
        }

    
        private String getUsername() throws IOException {
            //cannot use System.out.println cuz that would print the message on to terminal
            //instead I should use the output stream to put out a message on the client side
            out.println("Enter your username: ");
            //read the input from the input stream
            return in.readLine();
        }

        private void sendMessage(String message) {
            out.println(message);
            out.println("Enter your message: ");
        }

        public void setPair(ClientHandler pair) {
            this.pair = pair;
        }

        public ClientHandler getPair() {
            return this.pair;
        }

    }

}

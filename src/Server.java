import com.sun.deploy.util.SessionState;
import com.sun.xml.internal.bind.v2.TODO;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    List<User> clients = new ArrayList<User>();
    List<String> msgHistory = new ArrayList<String>();

    private Server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while(true) {
            User user = new User();
            user.socket = serverSocket.accept();
            clients.add(user);
            new Thread(new ClientHandler(this, user)).start();
        }
    }

    void broadcastMessages(String msg, List<User> clients, User msgAuthor) throws IOException {
        for (User client : clients ) {
            if (msgAuthor.socket != client.socket ) {
                PrintStream streamOut = new PrintStream(client.socket.getOutputStream());
                streamOut.println(msgAuthor.nickname + " - " + msg);
            }
        }
    }

    void sendMessagesHistory(List<String> msgHistory, User user) throws IOException {
        for (String string : msgHistory) {
            PrintStream streamOut = new PrintStream(user.socket.getOutputStream());
            streamOut.println(string);
        }
    }

    void greet(User user) throws IOException {
        PrintStream streamOut = new PrintStream(user.socket.getOutputStream());
        streamOut.println("You successful connected to chat! Please set uo your nickname with command (/nickname)");
    }

    void promtNickname(User user) throws IOException {
        PrintStream streamOut = new PrintStream(user.socket.getOutputStream());
        streamOut.println("Please set up your nickname with command (/nickname)");
    }

    public static void main(String[] args) throws IOException {
        new Server(8888);
    }
}

class ClientHandler implements Runnable {
    private Server serverSocket;
    private User client;

    ClientHandler(Server serverSocket, User user) {
        this.serverSocket = serverSocket;
        this.client = user;
    }

    @Override
    public void run() {
        String msg;


        try {
            this.serverSocket.sendMessagesHistory(this.serverSocket.msgHistory, this.client);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.serverSocket.greet(this.client);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Scanner sc = new Scanner(this.client.socket.getInputStream());
            while(sc.hasNextLine()) {
                msg = sc.nextLine();

                //System.out.println(msg);
                if (this.client.nickname == null) {
                    if (msg.contains("/nickname")) {
                        String nickname = msg.replace("/nickname", "").trim();

                        /// TODO: 4/15/2018 check nickname is empty
                        /// TODO: 4/15/2018 check nickname is free


                        this.client.nickname = nickname;
                    } else {
                        this.serverSocket.promtNickname(this.client);
                    }
                } else {
                    if (msg.contains("/nickname")) {
                        this.client.nickname = msg.replace("/nickname ", "");
                    } else {
                        this.serverSocket.msgHistory.add(this.client.nickname + " - " + msg);
                        this.serverSocket.broadcastMessages(msg, this.serverSocket.clients, this.client);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

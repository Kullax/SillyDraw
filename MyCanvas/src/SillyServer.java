import javax.net.ServerSocketFactory;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;

/**
 * Created by Martin on 02-02-2017.
 */
public class SillyServer{

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 2;
    private static final ClientHandler[] threads = new ClientHandler[maxClientsCount];

    public static void main(String[] args) throws IOException {
        ServerListener server = new ServerListener();
        server.start();

//        Socket socketToServer = new Socket("localhost", 15000);
//        ObjectOutputStream outStream = new ObjectOutputStream(socketToServer.getOutputStream());
//
//        for (int i=1; i<10; i++) {
//            try {
//                Thread.sleep((long) (Math.random()*3000));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Sending object to server ...");
//            outStream.writeObject(new MyLayer());
//        }
//        System.exit(0);
    }

    static class ServerListener  extends Thread{
        private ServerSocket serverSocket;

        ServerListener() throws IOException{
            serverSocket = ServerSocketFactory.getDefault().createServerSocket(15001);
        }

        @Override
        public void run(){
            while(true) {
                try {
                    System.out.print("Waiting!");
                    final Socket socketToClient = serverSocket.accept();
                    int i = 0;
                    for (i = 0; i < maxClientsCount; i++){
                        System.out.println(threads[i]);
                        if(threads[i] == null){
                            threads[i] = new ClientHandler(socketToClient, threads);
                            threads[i].start();
                            System.out.print("Assigning to " + i);
                            break;
                        }
                    }
                    if(i == maxClientsCount)
                    {
                        socketToClient.close();
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ClientHandler extends Thread{
        private Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outStream;

        ClientHandler(Socket socket, ClientHandler[] threads) throws IOException {
            this.socket = socket;
            inputStream = new ObjectInputStream(socket.getInputStream());
            outStream = new ObjectOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            System.out.println("Starting a Run");
            boolean running = true;
            while (running) {
                try {
                    Object o = inputStream.readObject();
                    if(o instanceof Stroke){
                        System.out.println("Read Stroke: "+((Stroke) o));
                        synchronized (this){
                            for(int i = 0; i < maxClientsCount; i++){
                                if(threads[i] != null && threads[i] != this){
                                    threads[i].outStream.writeObject(o);
                                }
                            }
                        }
                    }else
                        System.out.println("Read object: "+o);
                } catch (SocketException e){
                    running = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ending a Run");

            synchronized (this){
                for(int i = 0; i < maxClientsCount; i++){
                    if(threads[i] != this){
                        threads[i] = null;
                    }
                }
            }

            try {
                inputStream.close();
                outStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}





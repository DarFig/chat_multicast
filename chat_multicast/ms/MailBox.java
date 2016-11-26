package chat_multicast.ms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.SocketException;



public class MailBox extends Thread {

    
    private int port;
    private ServerSocket socket;
    private MonitorType<Envelope> queue;
    private boolean run = true;
	public MailBox(int p) {
		port = p;
		queue = new MonitorType<Envelope>();
	}
	
	public void run() {
		this.run = true;
		try {			
			socket = new ServerSocket(port);
            while(run){
            	
            	
            	ObjectInputStream inputStream = new ObjectInputStream(socket.accept().getInputStream());
            	queue.addLast((Envelope) inputStream.readObject());
            	inputStream.close();
            }
            socket.close();
		} catch (SocketException e) {
			System.err.println("Cerrando buzon.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
    
    public Envelope getNextMessage() {
        
        return queue.getFirst();
    }
    
    public void closeMailBox(){
    	this.run = false;
    }

	
}

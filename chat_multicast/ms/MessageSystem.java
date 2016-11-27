package chat_multicast.ms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;


public class MessageSystem {
	private int estampilla;
	private int source;
	private int pid;
	private boolean showDebugMsgs;
	private LinkedList<PeerAddress> addresses = new LinkedList<PeerAddress>();
	private MailBox mailbox;

	public MessageSystem(int source, String networkFile, boolean debug) throws FileNotFoundException {
		this.estampilla = 0;
		this.source = source;
		showDebugMsgs = debug;
		pid = source;
		int port = loadPeerAddresses(networkFile);
		mailbox = new MailBox(port);
		(new Thread (mailbox)).start();
	}
	
	public void send(int dst, Serializable message, int type) {
		if (showDebugMsgs) {
			System.out.println("Sending " + message.toString() + " from " + pid + " to " + dst);
		}
		try {
			Socket socket = addresses.get(dst-1).connect();
			
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			Envelope sobre = new Envelope(source,dst,message, type);
			outputStream.writeObject(sobre);
			outputStream.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMulticast(Serializable message, int type) {
		int dst = 1;
		for(PeerAddress i : addresses) {
			try {				
				Socket socket = i.connect();
					
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				Envelope sobre = new Envelope(source, dst, message, this.estampilla, type);
				outputStream.writeObject(sobre);
				outputStream.close();
				if (showDebugMsgs) {
					if(sobre.isMessage()){
						System.out.println("Estampilla: " + estampilla + ". Sending " + message.toString() + " from " + pid + " to " + dst);
					}else{
						System.out.println("Sending " + message.toString() + " from " + pid + " to " + dst);
					}
						
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dst++;
		}		
	}
	
	public Envelope receive() {
		Envelope mail = mailbox.getNextMessage();
		if(showDebugMsgs){
			System.out.println("Mensaje Recibido \"" + mail.getPayload() + "\" de " + mail.getSource());
		}
        return mail;
	}
	
	public void stopMailbox() {
		mailbox.closeMailBox();
	}
	
	public int getNumDst() {
		return addresses.size();
	}
	public int getPid(){
		return this.pid;
	}
	
	public int getStamp() {
		return this.estampilla;
	}
	
	public void setStamp(int stamp) {
		this.estampilla = stamp;
	}
	
	private int loadPeerAddresses(String networkFile) throws FileNotFoundException {
		BufferedReader in = new BufferedReader(new FileReader(networkFile));
		String line;
		int port = 0;
		int n = 0;
		try {
			while ((line = in.readLine()) != null) {
				++n;
				int sep = line.indexOf(':');
				if (sep != -1) {
					addresses.add(new PeerAddress(
							line.substring(0, sep),
							Integer.parseInt(line.substring(sep + 1))));
					if (n == pid) {
						port = addresses.getLast().port;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {}
		}
		
		return port;
	}
	
}

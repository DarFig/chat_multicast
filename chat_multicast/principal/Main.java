package chat_multicast.principal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import chat_multicast.ms.Envelope;
import chat_multicast.ms.MessageSystem;
import chat_multicast.ms.TotalOrderMulticast;

/**
 * @author Dariel
 */


public class Main {
	private static ChatDialog chat;
	private static TotalOrderMulticast multicast;
	
	public static void main(String[] args) {
		try {
			int id;
			String networkFile = "peers.txt";
			boolean debug = false;
			if(args[0].equals("-d")){
				debug = true;
				id = Integer.parseInt(args[1]);
				if(args.length > 2){
					networkFile = args[1];	
				}	
			} else {
				id = Integer.parseInt(args[0]);
				if(args.length > 1){
					networkFile = args[1];	
				}	
			}
			
			multicast = new TotalOrderMulticast(new MessageSystem(id, networkFile, debug));
			chat = new ChatDialog(new ActionListener(){
				public void actionPerformed(ActionEvent event){
					String mensaje = chat.text();
					multicast.sendMulticast(mensaje);
				}	
			});
			chat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Envelope sobre;
			while(true){
				sobre = multicast.receiveMulticast();
				chat.addMessage(sobre.getSource() + ": " + sobre.getPayload());
			}
			
		} catch(FileNotFoundException e){
			System.err.println("Fichero no encontrado");
		}
	}
		
}
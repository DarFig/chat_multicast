package chat_multicast.ms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class TotalOrderMulticast {
	private MessageSystem msystem;
	private int numAcks;
	private int numDst; 
	private boolean interesado;
	private int miEstampilla;
	
	private Semaphore enEnvio;
	private Semaphore mutex;
	
	private ArrayList<Integer> peticiones;
	
	private Serializable mensaje;
	
	
	public TotalOrderMulticast(MessageSystem ms) { 
		this.msystem = ms; 
		
		this.numDst = this.msystem.getNumDst();
		this.numAcks = 0;
		this.peticiones = new ArrayList<Integer>();
		this.interesado = false;
		this.miEstampilla = 0;
		this.enEnvio = new Semaphore(1);
		this.mutex = new Semaphore(1);
	}
	
	public void sendMulticast(Serializable message) {
		try {
			enEnvio.acquire();
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//enviar req
		this.interesado = true;
		this.mensaje = message;
		this.miEstampilla = this.msystem.getStamp()+1;//R1 stamp+1
		this.msystem.setStamp(this.miEstampilla);	 
		this.msystem.sendMulticast("request", 0);
		mutex.release();
	}
	public Envelope receiveMulticast() {
		
		while (true) {
			Envelope e = msystem.receive();
			if (e.isRequest()) {
				if(e.getSource() != e.getDestination() ){
					try {//queremos entrar en SC
						mutex.acquire();
					} catch (InterruptedException ec) {
						ec.printStackTrace();
					}
					if((e.getStamp() < this.miEstampilla) | (!interesado) |
						(e.getStamp() == this.miEstampilla && e.getSource() > e.getDestination())){
						this.msystem.send(e.getSource(), "ACK", 1);
					} else {
						peticiones.add(e.getSource());
					}
					mutex.release();
				} 
				
			} else if (e.isACK()) {
				this.numAcks++;
				if ( this.interesado && this.numAcks == this.numDst-1){
					this.msystem.sendMulticast(mensaje, 2);
					this.numAcks = 0;
					for(int i : peticiones){
						this.msystem.send(i, "ACK", 1);
					}
					try {
						mutex.acquire();
					} catch (InterruptedException ec) {
						ec.printStackTrace();
					}
					this.peticiones.clear();
					this.interesado = false;
					mutex.release();
					enEnvio.release();
				}
						
			} else {
				this.msystem.setStamp(Math.max(this.msystem.getStamp(), e.getStamp())+1);//R2
				return e;
			}
		}
	}
	

}

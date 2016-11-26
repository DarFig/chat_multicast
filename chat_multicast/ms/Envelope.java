package chat_multicast.ms;

import java.io.Serializable;

public class Envelope implements Serializable {
	private static final long serialVersionUID = 1L;
	private int source;
	private int destination;
	private Serializable payload;
	private int estampilla;
	private int type;
	
	public Envelope(int s, int d, Serializable p, int type) {
		source = s;
		destination = d;
		payload = p;
		this.type = type;
	}

	public Envelope(int s, int d, Serializable p, int estampilla, int type) {
		this.estampilla = estampilla;
		this.type = type;
		source = s;
		destination = d;
		payload = p;
	}
	
	public int getSource() { return source; }
	public int getDestination() { return destination; }
	public Serializable getPayload() { return payload; }
	
	public int getStamp() {
		return this.estampilla;
	}
	public boolean isRequest() {
		return this.type == 0;
	}
	public boolean isACK() {
		return this.type == 1;
	}
	public boolean isMessage() {
		return this.type == 2;
	}
}

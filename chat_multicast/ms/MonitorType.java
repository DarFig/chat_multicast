package chat_multicast.ms;


import java.util.ArrayList;




/**
 * 
 * @author Dariel
 *	
 * @param <T>
 */
public class MonitorType<T>{
	private ArrayList<T> queue;
	private int size = 10;
	
	public MonitorType(){
		this.queue = new ArrayList<T>(size);
	}
	public MonitorType(int max){
		this.size = max;
		this.queue = new ArrayList<T>(size);
	}
	
	public synchronized T getFirst() {
		while(queue.isEmpty()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return queue.remove(0);
	}
	
	public synchronized void addLast(T element) {
		if(queue.size() != size){
			queue.add(element);
			notifyAll();
		}
	}
	
}

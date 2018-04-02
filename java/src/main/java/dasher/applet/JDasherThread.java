package dasher.applet;
import java.util.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class JDasherThread extends Thread {

	private JDasherScreen screen;
	private Image backbuffer;
	private Image frontbuffer;
	private int state;
	private boolean frontbufferValid;
	/* Possible state values: 
	 * 0 - the backbuffer is not ready yet; the user should show the front.
	 * 1 - the backbuffer is ready for viewing; the user should show this,
	 *     flip the buffers and toggle this value.
	 * 2 - the thread should stop at the next possible opportunity.
	 */
	private Queue<DasherTasklet> events;
	
	private int width, height;
	
	public JDasherThread(JDasherScreen screen, JDasher dasher, int width, int height) {
		
		this.screen = screen;
		backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		frontbuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		frontbufferValid = false;
		this.width = width;
		this.height = height;
		events = new LinkedList<DasherTasklet>();
		
	}
	
	public synchronized void addTasklet(DasherTasklet t) {
		events.add(t);
		this.notifyAll();
	}
	
	private synchronized int getEvent() {

		while(this.state == 1 && events.isEmpty()) {
			try {
				wait();
			}
			catch(InterruptedException e) {
				return 2;
			}
		}
		
		if(!events.isEmpty()) {
			return 3;
		}
		else {		
			return this.state;
		}
		
	}
	
	public synchronized void setSize(int width, int height) {
		
		if(this.width == width && this.height == height) {
			return;
		}
		else {
			frontbufferValid = false;
			state = 0;
			backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			frontbuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			this.width = width;
			this.height = height;
			screen.setSize(width, height);
			this.notifyAll();
		}
		
	}
	
	public synchronized Image getCurrentFrontbuffer() {
		
		while(!frontbufferValid) {
			try { wait();} catch(InterruptedException e) {}
		}
			
		state = 0;
		this.notifyAll();
		return frontbuffer;
		
	}
	
	public void run() {
		
		while(true) {
			int event = getEvent();
			
			if(event == 2) return;
			// Thread should stop
			
			if(event == 0) {			
				screen.drawToComponent(backbuffer.getGraphics());
			
				synchronized(this) {
					Image temp = frontbuffer;
					frontbuffer = backbuffer;
					backbuffer = temp;
					frontbufferValid = true;
					state = 1;
					this.notifyAll();
				}
			}
			else if(event == 3) {
				DasherTasklet toRun = events.remove();
				toRun.run();
			}
			
		}
		
	}
	
}

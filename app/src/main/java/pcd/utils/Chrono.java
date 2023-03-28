package pcd.utils;

public class Chrono {

	private boolean running;
	private long startTime;

	public Chrono(){
		running = false;
	}
	
	public void start(){
		running = true;
		startTime = System.currentTimeMillis();
	}
	
	public void stop(){
		startTime = getTime();
		running = false;
	}

	public void stopAndPrintTime() {
		this.stop();
		System.out.println("[Log] Time elapsed: "+this.getTime()+" ms.");
	}
	
	public long getTime(){
		if (running){
			return 	System.currentTimeMillis() - startTime;
		} else {
			return startTime;
		}
	}
}

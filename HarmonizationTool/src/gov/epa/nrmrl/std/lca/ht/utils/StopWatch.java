package gov.epa.nrmrl.std.lca.ht.utils;

import java.util.concurrent.TimeUnit;

public class StopWatch {
	private String name= "";
	private long startTime;
	private long elapsedTime;
	boolean running = false;

	public StopWatch(String name) {
		this.name = name;
	}

	public void start() {
		startTime = System.nanoTime();
		running = true;
	}
	
	public long stop(){
		return stop(TimeUnit.MILLISECONDS);
	}

	public long stop(TimeUnit unit) {
		if (running) {
			elapsedTime += System.nanoTime() - startTime;
		}
		running = false;
		return unit.convert(elapsedTime, TimeUnit.NANOSECONDS);
	}
	
	public void reset(){
		elapsedTime = 0;
		if(running){
			start();
		}
	}

	public long getElapsedTime(TimeUnit unit) {
		if (running) {
			long elapsedTime = System.nanoTime() - startTime;
			return unit.convert(elapsedTime, TimeUnit.NANOSECONDS);
		} else {
			return unit.convert(elapsedTime, TimeUnit.NANOSECONDS);
		}
	}
	

	@Override
	public String toString() {
		return "StopWatch "+name+"[elapsedTime=" + getElapsedTime(TimeUnit.MILLISECONDS) + " millisec]";
	}

	public static void main(String[] args) {
		StopWatch stopWatch = new StopWatch("Test");
		stopWatch.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(stopWatch.stop(TimeUnit.MILLISECONDS)
				+ " milliseconds");
		stopWatch.start();
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(stopWatch.stop(TimeUnit.SECONDS) + " seconds");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(stopWatch.stop(TimeUnit.SECONDS) + " seconds");
	}
}

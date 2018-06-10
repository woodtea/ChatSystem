package testChat;

import java.io.*;

public class HeartBeatClient extends Thread {
	private volatile boolean stop = false;
	private volatile Integer count = 3;
	private ObjectOutputStream oos;

	HeartBeatClient(ObjectOutputStream oos) {
		this.oos = oos;
	}

	void reset() {
		synchronized (count) {
			count = 3;
		}
	}

	void stopThread() {
		stop = true;
	}

	@Override
	public void run() {
		try {
			while (!stop) {
				sleep(Parameter.ClientHeartBeatTime);
				synchronized (count) {
					count -= 1;
					if (count == 0) {
						synchronized(oos){
							IOControl.print(oos, new Message(0, ""));
						}
						count = 3;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("HeartBeatThread End!");
		}
	}
}

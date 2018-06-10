package testChat;

public class HeartBeatServer extends Thread {
	private volatile boolean stop = false;
	private volatile Integer count = 3;
	private ServerThread toStop = null;

	HeartBeatServer(ServerThread th) {
		this.toStop = th;
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
				sleep(Parameter.ServerHeartBeatTime);
				synchronized (count) {
					count -= 1;
					if (count == 0) {
						System.out.println("account already sign off!");
						stop = true;
						// TODO
						toStop.alreadySignIn = true;
						toStop.alreadySignOff = true;
						toStop.closeSocket();
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("HeartBeatServer end!");
		}
	}
}

package testChat;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class SendThread extends Thread{
	BlockingQueue<Message> queue;
	ObjectOutputStream oos;
	volatile boolean stop = false;
	private boolean isServer = false;
	private SendIdentifier si = new SendIdentifier();
	private HashSet<String>sendAck = new HashSet<String>();
	private ServerThread serverThread = null;
	private Server server = null;
	private String ClientID = null;
	
	SendThread(){
		queue = new LinkedBlockingQueue();
		oos = null;
	}
	
	SendThread(Server server, ServerThread st, String ClinetID, ObjectOutputStream oos){
		queue = new LinkedBlockingQueue();
		this.server = server;
		this.serverThread = st;
		this.ClientID = ClientID;
		this.oos = oos;
	}
	
	void put(Message msg){
		String msg_id = si.getID();
		msg.set_id(msg_id);
		try {
			queue.put(msg);
		} catch (InterruptedException e) {
			//TODO
			//上传到离线
			server.update_offline_message(ClientID, msg);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		while (!stop){
			try {
				Message to_send = queue.poll(100, TimeUnit.MILLISECONDS);
				
				if (to_send != null){
					//TODO
					//首先检查是否已经收到ACK,如果收到ACK则丢弃
					//否则发送,并将其放置到队列尾部(重发)
					boolean alreadyAck = false;
					synchronized(sendAck){
						alreadyAck = sendAck.contains(to_send.get_id());
					}
					if (alreadyAck == false)
					{
						IOControl.print(oos, to_send);
						new Thread(new Runnable() {
								public void run(){
									try {
										queue.put(to_send);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							).start();
					}
				}
			} catch (InterruptedException e) {
				stop = true;
				e.printStackTrace();
			} catch (Exception e){
				stop = true;
				e.printStackTrace();
			}
		}
		//将队列里还有的离线存储
		if (isServer){
			Message to_send = queue.poll();
			while (to_send != null){
				//TODO
				//将其加入离线
				server.update_offline_message(ClientID, to_send);
				to_send = queue.poll();
			}
		}
	}
	
	void setObjectOutputStream(ObjectOutputStream oos){
		this.oos = oos;
	}
	
	void setServer(){
		isServer = true;
	}
	
	public static void main(String[] args){
		SendThread st = new SendThread();
		st.start();
	}
}

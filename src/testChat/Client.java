package testChat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
	
	/**
	 * @param chat_name:聊天的名字，若是群聊，则为群名；否则为对方好友的名字
	 * @param isgroup:是否为群聊，true为是，false为否
	 * @return 该聊天是否为当前聊天，true为是，false为否
	 */
	boolean isCurrentChat(String chat_name, boolean isgroup) {
		//TODO 判断给定的聊天是否为当前主窗口中的聊天
		return true;
	}
	/**
	 * @param friend_name:发送信息者的用户名（可能是对方，也可能是自己）
	 * @param time:发从消息的时间,格式为year-month-date hour:minute:second
	 * @param self:发送信息者是否是自己，true为自己，false为不是
	 * @param info:发送的信息
	 */
	void showMessage(String friend_name, String time, boolean self, String info) {
		//TODO 这个函数用来在当前界面中显示信息，对方的信息在左侧显示，自己的信息在右侧显示
		
	}
	/**
	 * @param chat_name:聊天的名字，若是群聊，则为群名；否则为对方好友的名字
	 * @param isgroup:是否为群聊，true为是，false为否
	 */
	void remindMessage(String chat_name, boolean isgroup) {
		//TODO 将指定的聊天（并非当前聊天）移动到好友列表的第一个，并显示一个红点
		
	}
	
	
	
	class RecieveThread extends Thread {
		ObjectInputStream ois;

		RecieveThread(ObjectInputStream ois) {
			this.ois = ois;
		}

		public void run() {
			while (true) {
				Message msg = IOControl.read(ois);

				int type = msg.get_type();
				String from = msg.get_from();
				String to = msg.get_to();
				boolean isgroup = msg.get_isgroup();
				String info = msg.get_msg();

				if (type == 6) {
					synchronized(requestFriend){
						requestFriend.put(id, true);
					}
				}
				if (type == 7) {
				}
				
				if (type == 8) {
					int fromid=Integer.parseInt(from);
					if (id2name.containsKey(fromid))
						System.out.println("("+id2name.get(fromid)+") "+info);
					else
						System.out.println("( id: "+fromid+" ) "+info);
				}
				
				if (type == 9) {
					parseFriend(info);
				}
				
				if (type != 8)
				System.out.println(
						"type:" + type + ", from:" + from + ", to:" + to + ", isgroup:" + isgroup + ", msg:" + info);
			}
		}
	}
	
	private String name;
	private Integer id;
	private ConcurrentHashMap<String, Integer> name2id;
	private ConcurrentHashMap<Integer, String> id2name;
	private HashMap<Integer, Boolean>requestFriend;

	private void parse_information(String msg) {
		msg.split("\n");
	}

	private Client() throws Exception {
		OutputStream os = null;
		PrintWriter pw = null;
		ObjectOutputStream oos = null;
		
		boolean inChatRoom=false;
		String defaultDes="";

		InputStream is = null;
		BufferedReader br = null;
		ObjectInputStream ois = null;

		Scanner sc = null;
		Socket socket = null;
		
		name2id = new ConcurrentHashMap<String, Integer>();
		id2name = new ConcurrentHashMap<Integer, String>();
		requestFriend = new HashMap<Integer, Boolean>();

		try {
			String host = InetAddress.getLocalHost().getHostAddress();
			System.out.println(host);
			int port = 1234;

			socket = new Socket(host, port);

			os = socket.getOutputStream();
			pw = new PrintWriter(os);
			oos = new ObjectOutputStream(os);

			is = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			ois = new ObjectInputStream(is);

			sc = new Scanner(System.in);

			boolean alreadySignIn = false;

			while (alreadySignIn == false) {
				int type = sc.nextInt();
				sc.nextLine();

				String tmp = null;
				if (type != 3)
					tmp = sc.nextLine();
				if (can_send(type, 0, tmp))
					IOControl.print(oos, new Message(type, tmp));
				else
					continue;

				if (type == 3)
					return;

				Message msg = IOControl.read(ois);

				type = msg.get_type();
				String info = msg.get_msg();
				System.out.println("(Server) " + info);
				if (type == 4) {
					alreadySignIn = true;
					String[] user2passwd = tmp.split(" ");
					name = user2passwd[0];
					
					parseInfo(info);
				}
			}
			
			RecieveThread recieve = new RecieveThread(ois);
			recieve.start();
			
			boolean alreadySignOff = false;

			while (alreadySignOff == false) {
				
				if (inChatRoom){
					String inp=sc.nextLine();
					if (inp.equals("\\quit"))
					{
						inChatRoom=false;
						defaultDes="";
					}else{
						Message new_msg=new Message(8, id.toString(), defaultDes, false , inp);
						IOControl.print(oos, new_msg);
					}
					continue;
				}
				
				int type = sc.nextInt();
				if (type==1001){
					list_friend();
					continue;
				}
				
				if (type==3){
					IOControl.print(oos, new Message(3, id.toString(), "0", false, ""));
					alreadySignOff=false;
				}
				sc.nextLine();
				
				String to = sc.nextLine();
				int toID=Integer.parseInt(to);
				
				String info = sc.nextLine();
				Message new_msg = new Message(type, id.toString(), to, false, info);
				if (can_send(type, toID, info))
					IOControl.print(oos, new_msg);
				
				if (type==7){
					IOControl.print(oos, new Message(9,id.toString(),"",false,""));
				}
				if (type==8){
					inChatRoom=true;
					defaultDes=to;
					System.out.println("Start chat");
				}
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ois != null)
				ois.close();
			if (oos != null)
				oos.close();
			if (br != null)
				br.close();
			if (is != null)
				is.close();
			if (pw != null)
				pw.close();
			if (os != null)
				os.close();
			if (socket != null)
				socket.close();
			System.out.println("end!");
		}
	}
	
	private void parseInfo(String info) {
		String tmp[] = info.split("\n");
		parseFriend(tmp[0]);
	}
	
	private void parseFriend(String info){
		name2id.clear();
		id2name.clear();
		String fri[] = info.split(" ");
		assert fri.length % 2 == 1;
		id=Integer.parseInt(fri[0]);
		for(int i=1;i<fri.length;i+=2) {
			name2id.put(fri[i+1], Integer.parseInt(fri[i]));
			id2name.put(Integer.parseInt(fri[i]), fri[i+1]);
		}
	}
	
	private boolean can_send(int type, int id, String msg){
		switch(type){
			case 1:
			case 2:
				int SpaceCount=0,nCount=0,len=msg.length();
				for (int i=0;i<len;++i)
				{
					if (msg.charAt(i)==' ')
						SpaceCount++;
					if (msg.charAt(i)=='\n')
						nCount++;
				}
				if (SpaceCount!=1||nCount>0)
				{
					System.out.println("no space and '\n' in account name or password!");
					return false;
				}
				return true;
			case 3:
				return true;
			case 6:
				if (id2name.containsKey(id)){
					System.out.println("you are already friends!");
					return false;
				}
				return true;
			case 7:
				boolean result=false;
				synchronized(requestFriend)
				{
					if (requestFriend.containsKey(id))
					{
						requestFriend.remove(id);
						result=true;
					}
					else
					{
						System.out.println("Invalid accept packet!");
					}
				}
				return result;
			case 8:
				if (id2name.containsKey(id)==false){
					System.out.println("you are not friends!");
					return false;
				}
				return true;
			case 9:
				return true;
			default:
				return false;
		}
	}
	
	private void list_friend(){
		for (Map.Entry<String, Integer>e: name2id.entrySet())
			System.out.println(e.getValue()+" "+e.getKey());
	}

	public static void main(String[] args) throws Exception {
		Client myClient = new Client();
	}
}

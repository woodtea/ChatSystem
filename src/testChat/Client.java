package testChat;

import gui.Functions;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

public class Client extends Thread {
	
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
					Functions.showAddFriendRequest(from, info);
				}
				if (type == 7) {
					Functions.showAddFriendReply(from, info);
				}
				
				if (type == 8) {
					//type==8需要解密
					info = SecurityCipher.get_receive(alice, info);
					
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	//设置日期格式
					String time = df.format(new Date());
					if(isgroup) {	//是群聊
						String group_name = group_id2name.get(Integer.parseInt(from.split("_")[1]));
						String friend_name = id2name.get(Integer.parseInt(from.split("_")[0]));
						ImageIcon profile = null;
						Functions.showGroupMessage(group_name, friend_name, profile, time, info);
					}
					else {	//不是群聊
						String friend_name = id2name.get(Integer.parseInt(from));
						ImageIcon profile = null;
						Functions.showFriendMessage(friend_name, profile, time, info);
					}
				}
				
				if (type == 9) {
					parseFriend(info);
					
				}
				
				// type == 10, 删除好友信息包不会被转发给用户，所以客户端不可能收到type == 10的包
				
				if(type == 11) {
					
				}
				if(type == 12) {
					
				}
				if(type == 13) {
					
				}
				if(type == 14) {
					
				}
				if(type == 15) {
					
				}
				if(type == 16) {
					parseGroup(info);
					
				}
				if(type == 17) {
					int messageNumber = Integer.parseInt(info.substring(0, info.indexOf("_")));
					String reply = info.substring(info.indexOf("_") + 1, info.length());
					Functions.replyMsg(Integer.parseInt(from), Integer.parseInt(to), isgroup, messageNumber, reply);
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
	private ConcurrentHashMap<Integer, User> friendList;
	private ConcurrentHashMap<String, Integer> group_name2id;
	private ConcurrentHashMap<Integer, String> group_id2name;
	private HashMap<Integer, Boolean>requestFriend;
	private boolean alreadySignIn = false;
	private boolean alreadySignOff = false;
	private OutputStream os = null;
	private PrintWriter pw = null;
	private ObjectOutputStream oos = null;
	/*测试用代码
	private boolean inChatRoom=false;
	private String defaultDes="";
	*/
	private InputStream is = null;
	private BufferedReader br = null;
	private ObjectInputStream ois = null;
	
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private byte[] alice;
	
	private Socket socket = null;
	
	public String get_name() {
		return name;
	}
	public Integer get_id() {
		return id;
	}
	public ConcurrentHashMap<String, Integer> getName2id() {
		return name2id;
	}
	public ConcurrentHashMap<Integer, String> getId2name() {
		return id2name;
	}
	public ConcurrentHashMap<String, Integer> getGroup_name2id() {
		return group_name2id;
	}
	public ConcurrentHashMap<Integer, String> getGroup_id2name() {
		return group_id2name;
	}
	
	public Client() {
		/*测试用代码
		Scanner sc = null;
		*/
		name2id = new ConcurrentHashMap<String, Integer>();
		id2name = new ConcurrentHashMap<Integer, String>();
		requestFriend = new HashMap<Integer, Boolean>();
		group_name2id = new ConcurrentHashMap<String, Integer>();
		group_id2name = new ConcurrentHashMap<Integer, String>();
	}
	
	public void run(){
		
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
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			alice = SecurityCipher.GenerateKey_Client(dis, dos);
			
			//sc = new Scanner(System.in);

			while (alreadySignIn == false) {
				/*	测试用代码
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
				*/
			}
			System.out.println("(client) already sign in.");
			
			RecieveThread recieve = new RecieveThread(ois);
			recieve.start();

			while (alreadySignOff == false) {
				/* 测试用代码
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
				*/
			}
			System.out.println("(client) already sign off.");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
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
				if (dis != null)
					dis.close();
				if (socket != null)
					socket.close();
				System.out.println("end!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String signUp(String user_name, String password) {
		if (can_send(1, 0, user_name + " " + password)) {
			
			IOControl.print(oos, new Message(1, SecurityCipher.get_send(alice, user_name + " " + password)));
			Message msg = IOControl.read(ois);
			int type = msg.get_type();
			String info = msg.get_msg();
			return type + "_" + info;
		}
		else
			return "fail to sign up!";
	}
	
	public String signIn(String user_name, String password) {
		if (can_send(2, 0, user_name + " " + password)) {
			
			IOControl.print(oos, new Message(2, SecurityCipher.get_send(alice, user_name + " " + password)));
			Message msg = IOControl.read(ois);
			int type = msg.get_type();
			String info = msg.get_msg();
			if(type == 4) {
				alreadySignIn = true;
				name = user_name;
				parseInfo(info);	//解析好友列表,群列表
			}
			return type + "_" + info;
		}
		else
			return "no space and '\n' in account name or password!";
	}
	
	public void signOff() {
		alreadySignIn = true;
		alreadySignOff = true;
	}
	
	public ConcurrentHashMap<Integer, Functions.user> getFriendList(){
		ConcurrentHashMap<Integer, Functions.user> list = new ConcurrentHashMap<Integer, Functions.user>();
		for(Integer i : friendList.keySet()) {
			list.put(i, new Functions.user(friendList.get(i).name, i, friendList.get(i).image));
		}
		return list;
	}
	
	public ConcurrentHashMap<Integer, Functions.group> getGroupList(){
		ConcurrentHashMap<Integer, Functions.group> list = new ConcurrentHashMap<Integer, Functions.group>();
		for(Integer i : group_id2name.keySet()) {
			list.put(i, new Functions.group(group_id2name.get(i), i));
		}
		return list;
	}
	
	public void sendMessage(Integer from, Integer to, Integer messageNumber, boolean isGroup, String info) {
		String send = SecurityCipher.get_send(alice, messageNumber.toString()+"_"+info);
		IOControl.print(oos, new Message(8, from.toString(), to.toString(), isGroup, send));
	}
	
	
	private void parseInfo(String info) {
		String tmp[] = info.split("\n");
		parseFriend(tmp[0]);
		if(tmp.length == 1)
			parseGroup("");
		else
			parseGroup(tmp[1]);
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
	
	private void parseGroup(String info) {
		group_name2id.clear();
		group_id2name.clear();
		if(info.equals(""))
			return;
		String gru[] = info.split(" ");
		assert gru.length % 2 == 0;
		for(int i=0;i<gru.length;i+=2) {
			group_name2id.put(gru[i+1], Integer.parseInt(gru[i]));
			group_id2name.put(Integer.parseInt(gru[i]), gru[i+1]);
		}
	}
	
	private boolean can_send(int type, int id, String msg){
		switch(type){
			case 1:
				return true;
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
		myClient.start();
	}
}

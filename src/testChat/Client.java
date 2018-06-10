package testChat;

import gui.Functions;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

public class Client extends Thread {

	class ReceiveThread extends Thread {
		ObjectInputStream ois;
		boolean stop = false;

		ReceiveThread(ObjectInputStream ois) {
			this.ois = ois;
		}

		@SuppressWarnings("unused")
		public void run() {
			try {
				while (!stop) {
					Message msg = null;
					msg = (Message) IOControl.read(ois);

					if (msg == null) {
						System.out.println("Null Class!");
						return;
					}

					if (msg.get_type() != 9 || Parameter.isTerminal == false)
						System.out.println("(receive thread) " + msg.toString());

					int type = msg.get_type();
					String from = msg.get_from();
					String to = msg.get_to();
					boolean isgroup = msg.get_isgroup();
					String info = msg.get_msg();

					if (type == 6) {
						synchronized (requestFriend) {
							requestFriend.put(from, true);
						}
						// 显示加好友请求
						if (Parameter.isTerminal == false)
							Functions.showAddFriendRequest(from, info);
					}
					/*
					 * 显示加好友回复 from : 回复请求者的name + profile to : 自己的name info :
					 */
					if (type == 7 && Parameter.isTerminal == false) {
						String new_name = from.substring(0, from.indexOf("_"));
						int new_id = Integer.parseInt(from.substring(from.indexOf("_") + 1, from.indexOf("<profile>")));
						ImageIcon new_profile = new ImageIcon(ImageControl.base64StringToImg(
								from.substring(from.indexOf("<profile>") + 9, from.indexOf("</profile>"))));
						if (info.equals("1")) {
							addNewFriend(new_name, new_id, new_profile);
							
							Functions.showAddFriendReply(new_name, new_id, new_profile, true);
							
							// Functions.showAddFriendReply(new_name, new_id,
							// new_profile, true);
						} else {
							Functions.showAddFriendReply(new_name, new_id, new_profile, false);
							// Functions.showAddFriendReply(new_name, new_id,
							// new_profile, false);
						}

					}

					if (type == 8) {
						// 接收对方发送的消息，需要解密
						info = SecurityCipher.get_receive(alice, info);

						if (Parameter.isTerminal == false) {
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
							String time = df.format(new Date());
							if (isgroup) { // 是群聊
								int group_id = Integer
										.parseInt(from.substring(from.indexOf("_") + 1, from.indexOf("<profile>")));
								String speaker_name = from.substring(0, from.indexOf("_"));
								ImageIcon profile = new ImageIcon(ImageControl.base64StringToImg(
										from.substring(from.indexOf("<profile>") + 9, from.indexOf("</profile>"))));
								Functions.showGroupMessage(group_id, speaker_name, profile, time, info);
							} else { // 不是群聊
								int friend_id = Integer.parseInt(from);
								Functions.showFriendMessage(friend_id, time, info, 
								          friendList.get(friend_id).get_icon(), friendList.get(friend_id).get_name());
							}
						}
					}

					if (type == 9) {
						// 解析好友列表
						if (Parameter.isTerminal == false)
							parseFriendWithProfile(info);
						else
							parseFriend(info);
					}

					// type == 10, 删除好友信息包不会被转发给用户，所以客户端不可能收到type == 10的包

					if (type == 11) {
						// 显示新群
						if (Parameter.isTerminal == false) {
							int owner = Integer.parseInt(from);
							String group_name = info.split("_")[0];
							int group_id = Integer.parseInt(info.split("_")[1]);

							addNewGroup(group_name, group_id, owner);

							
						}
					}
					if (type == 12) {
						// 将被解散的群删除
						if (Parameter.isTerminal == false) {
							deleteGroup(Integer.parseInt(to));
						}
					}
					if (type == 13) {
						// 被邀请入群，to:邀请的用户+邀请其加入的群id+邀请其加入的群name+邀请其加入的群owner
						if (Parameter.isTerminal == false) {
							String tmp[] = to.split("_");
							int owner = Integer.parseInt(tmp[3]);
							String group_name = tmp[2];
							int group_id = Integer.parseInt(tmp[1]);

							addNewGroup(group_name, group_id, owner);

							
						}
					}
					if (type == 14) {
						// 退群成功
						if (Parameter.isTerminal == false)
							deleteGroup(Integer.parseInt(to));
					}
					if (type == 15) {
						// 被踢
						if (Parameter.isTerminal == false) {
							int delete_id = Integer.parseInt(to.split("_")[1]);
							deleteGroup(delete_id);
						}
					}
					if (type == 16) {
						if (Parameter.isTerminal == false)
							parseGroupWithOwner(info);
					}
					if (type == 17) {
						if (Parameter.isTerminal == false) {
							int messageNumber = Integer.parseInt(info.substring(0, info.indexOf("_")));
							String reply = info.substring(info.indexOf("_") + 1, info.length());
							Functions.replyMsg(Integer.parseInt(from), Integer.parseInt(to), isgroup, messageNumber,
									reply);
						}
					}
					
					if (type == 18) {
						if (Parameter.isTerminal){
							String[] msg_array = info.split("\n");
							System.out.println("type:" + type + ", from:" + from +
									", to:" + to + ", isgroup:" + isgroup );
							for (String msg_info : msg_array){
								String tmp_info = SecurityCipher.get_receive(alice, msg_info);
								System.out.println(tmp_info);
							}
						}
					}

					if (Parameter.isTerminal == true && type != 18)
						System.out.println("(again)" + info);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.out.println("Receive Thread end!");
			}
		}
	}

	private String name;
	private Integer id;
	private ImageIcon icon;
	private ConcurrentHashMap<String, Integer> name2id;
	private ConcurrentHashMap<Integer, String> id2name;
	private volatile ConcurrentHashMap<Integer, Functions.user> friendList;
	// private ConcurrentHashMap<String, Integer> group_name2id;
	private ConcurrentHashMap<Integer, String> group_id2name;
	private volatile ConcurrentHashMap<Integer, Functions.group> groupList;

	private HashMap<String, Boolean> requestFriend;

	private volatile boolean alreadySignIn = false;
	private volatile boolean alreadySignOff = false;
	private OutputStream os = null;
	private PrintWriter pw = null;
	private ObjectOutputStream oos = null;
	/*
	 * 测试用代码 private boolean inChatRoom=false; private String defaultDes="";
	 */
	private InputStream is = null;
	private BufferedReader br = null;
	private volatile ObjectInputStream ois = null;

	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private byte[] alice;

	private Socket socket = null;

	private ReceiveThread receive;

	public String get_name() {
		return name;
	}

	public Integer get_id() {
		return id;
	}

	public ImageIcon get_icon() {
		return icon;
	}

	public ConcurrentHashMap<String, Integer> getName2id() {
		return name2id;
	}

	public ConcurrentHashMap<Integer, String> getId2name() {
		return id2name;
	}

	public ConcurrentHashMap<Integer, String> getGroup_id2name() {
		return group_id2name;
	}

	public Client() {
		/*
		 * 测试用代码 Scanner sc = null;
		 */
		name2id = new ConcurrentHashMap<String, Integer>();
		id2name = new ConcurrentHashMap<Integer, String>();
		friendList = new ConcurrentHashMap<Integer, Functions.user>();
		requestFriend = new HashMap<String, Boolean>();
		// group_name2id = new ConcurrentHashMap<String, Integer>();
		group_id2name = new ConcurrentHashMap<Integer, String>();
		groupList = new ConcurrentHashMap<Integer, Functions.group>();
	}

	public void run() {

		Scanner sc = null;
		HeartBeatClient hbt = null;
		
		try {
			String host = "";
			if (Parameter.isLocal)
				host = InetAddress.getLocalHost().getHostAddress();
			else
				host = Parameter.Server_IP;
			System.out.println(host);
			int port = Parameter.Server_Port;

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
			
			if (Parameter.isTerminal){
				hbt = new HeartBeatClient(oos);
				hbt.start();
			}

			sc = new Scanner(System.in);

			while (alreadySignIn == false) {
				// /* 测试用代码
				if (Parameter.isTerminal) {
					int type = sc.nextInt();
					sc.nextLine();

					if (type >= 3) {
						System.out.println("wrong type!");
						continue;
					}

					String tmp = null;
					if (type != 3)
						tmp = sc.nextLine();
					tmp = SecurityCipher.get_send(alice, tmp);
					if (can_send(type, 0, tmp)) {
						synchronized (oos) {
							hbt.reset();
							IOControl.print(oos, new Message(type, tmp));
						}
					} else
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
				// */
			}
			System.out.println("(client) already sign in.");

			if (Parameter.isTerminal) {
				ReceiveThread recieve = new ReceiveThread(ois);
				recieve.start();
			}

			while (alreadySignOff == false) {
				// /* 测试用代码
				if (Parameter.isTerminal) {

					int type = sc.nextInt();
					if (type == 1001) {
						list_friend();
						continue;
					}

					if (type == 3) {
						synchronized (oos) {
							hbt.reset();
							IOControl.print(oos, new Message(3, id + "", "0", false, ""));
						}
						alreadySignOff = false;
						continue;
					}

					sc.nextLine();

					String to = sc.nextLine();
					int toID = 0;
					if (type != 6 && type != 7 && type != 8 && type != 11)
						toID = Integer.parseInt(to);

					boolean isgroup = false;
					if (type == 8 || type == 18) {
						String t = sc.nextLine();
						if (t.equals("Group"))
							isgroup = true;
					}

					String info = sc.nextLine();
					if (type == 8 || type == 18)
						info = SecurityCipher.get_send(alice, info);
					Message new_msg = new Message(type, id + "", to, isgroup, info);
					if (type == 7 && can_send(type, to, info)) {
						synchronized (oos) {
							hbt.reset();
							IOControl.print(oos, new_msg);
						}
					} else if (can_send(type, toID, info)) {
						synchronized (oos) {
							hbt.reset();
							IOControl.print(oos, new_msg);
						}
					}
					if (type == 7) {
						synchronized (oos) {
							hbt.reset();
							IOControl.print(oos, new Message(9, id.toString(), "", false, ""));
						}
					}
				}
				// */
			}
			System.out.println("(client) already sign off.");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (receive != null)
				receive.stop = true;
			if (hbt != null)
				hbt.stopThread();
			try {
				if (sc != null)
					sc.close();
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
			synchronized (oos) {
				try {
					IOControl.print(oos, new Message(1, SecurityCipher.get_send(alice, user_name + " " + password)));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			Message msg = null;
			try {
				msg = IOControl.read(ois);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int type = msg.get_type();
			String info = msg.get_msg();
			return type + "_" + info;
		} else
			return "fail to sign up!";
	}

	public String signIn(String user_name, String password) {
		if (can_send(2, 0, user_name + " " + password)) {
			synchronized (oos) {
				try {
					IOControl.print(oos, new Message(2, SecurityCipher.get_send(alice, user_name + " " + password)));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			Message msg = null;
			try {
				msg = IOControl.read(ois);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int type = msg.get_type();
			String info = msg.get_msg();
			if (type == 4) {
				alreadySignIn = true;
				name = user_name;
				parseInfo(info); // 解析好友列表,群 + 解析好友列表后，客户端会从服务器得到所有人的头像，所有群的群主

				receive = new ReceiveThread(ois);
				receive.start();
				synchronized (oos) {
					try {
						IOControl.print(oos, new Message(9, id + "", "", false, ""));
						IOControl.print(oos, new Message(16, id + "", "", false, ""));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					this.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return type + "_" + info;
		} else
			return "no space and '\n' in account name or password!";
	}

	public void signOff() {
		alreadySignIn = true;
		alreadySignOff = true;
	}

	public ConcurrentHashMap<Integer, Functions.user> getFriendList() {
		return friendList;
	}

	public ConcurrentHashMap<Integer, Functions.group> getGroupList() {
		return groupList;
	}

	public void sendMessage(Integer from, Integer to, Integer messageNumber, boolean isGroup, String info) {
		System.out.println("(before cipher)" + messageNumber + "_" + info);
		String send = SecurityCipher.get_send(alice, messageNumber + "_" + info);
		System.out.println("(after cipher)" + send);
		synchronized (oos) {
			try {
				IOControl.print(oos, new Message(8, from.toString(), to.toString(), isGroup, send));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void parseInfo(String info) {
		String tmp[] = info.split("\n");
		parseFriend(tmp[0]);
		if (tmp.length == 1)
			parseGroup("");
		else
			parseGroup(tmp[1]);
	}

	private void parseFriend(String info) {
		name2id.clear();
		id2name.clear();
		String fri[] = info.split(" ");
		assert fri.length % 2 == 1;
		id = Integer.parseInt(fri[0]);
		for (int i = 1; i < fri.length; i += 2) {
			name2id.put(fri[i + 1], Integer.parseInt(fri[i]));
			id2name.put(Integer.parseInt(fri[i]), fri[i + 1]);
		}
	}

	private void parseFriendWithProfile(String result) {
		String fri[] = result.split("</profile>");
		icon = new ImageIcon(
				ImageControl.base64StringToImg(fri[0].substring(fri[0].indexOf("<profile>") + 9, fri[0].length())));
		for (int i = 1; i < fri.length; i++) {
			String fri_name = fri[i].substring(0, fri[i].indexOf(" "));
			String fri_id = fri[i].substring(fri[i].indexOf(" ") + 1, fri[i].indexOf("<profile>"));
			ImageIcon fri_icon = new ImageIcon(
					ImageControl.base64StringToImg(fri[i].substring(fri[i].indexOf("<profile>") + 9, fri[i].length())));
			friendList.put(Integer.parseInt(fri_id), new Functions.user(fri_name, Integer.parseInt(fri_id), fri_icon));
		}
	}

	private void parseGroup(String info) {
		// group_name2id.clear();
		group_id2name.clear();
		if (info.equals(""))
			return;
		String gru[] = info.split(" ");
		assert gru.length % 2 == 0;
		for (int i = 0; i < gru.length; i += 2) {
			// group_name2id.put(gru[i+1], Integer.parseInt(gru[i]));
			group_id2name.put(Integer.parseInt(gru[i]), gru[i + 1]);
		}
	}

	private void parseGroupWithOwner(String result) {
		// System.out.println("safasfasfasfas");
		groupList.clear();
		if (result.equals(""))
			return;
		String gru[] = result.split("\n");

		ImageIcon icon = Functions.group_profile;

		for (int i = 0; i < gru.length; i++) {
			String tmp[] = gru[i].split(" ");
			groupList.put(Integer.parseInt(tmp[1]),
					new Functions.group(tmp[0], Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), icon));
		}
	}

	void addNewGroup(String name, Integer id, int owner) {
		// group_name2id.put(name, id);
		group_id2name.put(id, name);

		ImageIcon icon = Functions.group_profile;

		groupList.put(id, new Functions.group(name, id, owner, icon));
	}

	void addNewFriend(String name, Integer id, ImageIcon profile) {
		name2id.put(name, id);
		id2name.put(id, name);
		friendList.put(id, new Functions.user(name, id, profile));
	}

	void deleteGroup(Integer id) {
		id2name.remove(id);
		groupList.remove(id);
	}

	private boolean can_send(int type, String id, String msg) {
		boolean result = false;
		synchronized (requestFriend) {
			if (requestFriend.containsKey(id)) {
				requestFriend.remove(id);
				result = true;
			} else {
				System.out.println("Invalid accept packet!");
			}
		}
		return result;
	}

	private boolean can_send(int type, int id, String msg) {
		switch (type) {
		case 1:
		case 2:
			int SpaceCount = 0, nCount = 0, len = msg.length();
			for (int i = 0; i < len; ++i) {
				if (msg.charAt(i) == ' ')
					SpaceCount++;
				if (msg.charAt(i) == '\n')
					nCount++;
			}
			if (SpaceCount != 1 || nCount > 0) {
				System.out.println("no space and '\n' in account name or password!");
				return false;
			}
			String[] tmp_split = msg.split(" ");
			
			if (type == 1 && !SecurityCipher.password_strength_check(tmp_split[1]))
			{
				System.out.println("password is not strong enough!");
				return false;
			}
			return true;
		case 3:
			return true;
		case 6:
			if (name2id.containsKey(id)) {
				System.out.println("you are already friends!");
				return false;
			}
			return true;
		case 7:
			return true;
		case 8:
			return true;
		case 9:
			return true;
		default:
			return true;
		}
	}

	private void list_friend() {
		for (Map.Entry<String, Integer> e : name2id.entrySet())
			System.out.println(e.getValue() + " " + e.getKey());
	}
	public void sendAddFriendRequest(Integer self_id, String new_friend_name, String info)  {
		try {
			IOControl.print(oos, new Message(6, self_id.toString(), new_friend_name, false, info));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendAddFriendReply(String new_friend, boolean accept)  {
		if(accept)
			try {
				IOControl.print(oos, new Message(7, id+"", new_friend, false, "1"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			try {
				IOControl.print(oos, new Message(7, id+"", new_friend, false, "0"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static void main(String[] args) throws Exception {
		Client myClient = new Client();
		myClient.start();
	}
}
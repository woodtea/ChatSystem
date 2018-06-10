package gui;

import testChat.Client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author cmy
 * 如果需要返回其它的内容，请自行在该类中添加static字段，
 * 并在"需要填充的内容"下面把返回的内容做好说明
 */
public class Functions {
	
	static String inform = "测试用字符串";
	static user hostUser = new user(1);
	static Client client;
	
	static int messageNumber = 0;
	static HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> replyList
		=new HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>>();
	
	public static final String registerSuccess = "成功注册！";
	public static final String signInSuccess = "成功登陆！";
		
	public static final String success = "发送成功！";
	public static final String timeOut = "发送超时！";
	public static final String notFriend = "对方不是您的好友！";
	public static final String notGroupMember = "您已不是该群的成员！";
	public static final String nulltext = "空信息不能发送！";
	public static final ImageIcon group_profile = new ImageIcon("newFriend.png");
	
	public static void setClient(Client new_client) {
		client = new_client;
	}
	
	public static void setHostUser(String name, int id, ImageIcon icon){
		hostUser = new user(name, id, icon);
	}
	
	public static void signOff() {
		//TODO 通知后端结束进程
		client.signOff();
	}
	
	/**
	 * @param from 发送者自己的id
	 * @param to 发送对象的id（可能是用户id，也可能是群id）
	 * @param isGroup 聊天是否是群聊
	 * @param text	聊天信息
	 * @return 发送是否成功
	 */
	public static String sendMsg(int from, int to, boolean isGroup, String text)
	{
		//TODO
		if(text.equals(""))
			   return nulltext;
		System.out.println("(Functions sendMsg)"+from+" "+to+" "+isGroup+" "+text);
		if(! replyList.containsKey(from)) 
			replyList.put(from, new HashMap<Integer, HashMap<Integer, String>>());
		if(! replyList.get(from).containsKey(to))
			replyList.get(from).put(to, new HashMap<Integer, String>());
		int tmpNumber = messageNumber;
		messageNumber += 1;
		
		replyList.get(from).get(to).put(tmpNumber, "wait");
		client.sendMessage(from, to, tmpNumber, isGroup, text);
		
		waitReply(from, to, tmpNumber);
		while(replyList.get(from).get(to).get(tmpNumber).equals("wait")) {
			//等待reply,或超时
		}
		String reply = replyList.get(from).get(to).get(tmpNumber);
		replyList.get(from).get(to).remove(tmpNumber);
		
		return reply;
	}
	
	public static void waitReply(int from, int to, int messageNumber) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				if(! replyList.containsKey(from))
					return;
				if(! replyList.get(from).containsKey(to))
					return;
				if(! replyList.get(from).get(to).containsKey(messageNumber))
					return;
				replyList.get(from).get(to).put(messageNumber, timeOut);
			}
		} , 3000);
	}
	
	public static void replyMsg(int from, int to, boolean isGroup, int messageNumber, String reply) {
		if(! replyList.containsKey(from))
			return;
		if(! replyList.get(from).containsKey(to))
			return;
		if(! replyList.get(from).get(to).containsKey(messageNumber))
			return;
		replyList.get(from).get(to).put(messageNumber, reply);
	}
	
	/**
	 * @param usrName 用户名
	 * @param password 密码
	 * @return 登录操作是否成功
	 * 			需要填充的内容:
	 * 			hostUser:用户		
	 * 		
	 */
	public static boolean log(String usrName,char[] password)
	{
		//TODO
		String info = client.signIn(usrName, new String(password));
		if(info.indexOf("_") != -1) {
			int type = Integer.parseInt(info.substring(0, info.indexOf("_")));
			if(type == 5) {
				inform = info.substring(info.indexOf("_")+1, info.length());
				return false;
			}
			else {
				inform = signInSuccess;
				setHostUser(client.get_name(), client.get_id(), client.get_icon());
				return true;
			}
		}
		else 
			inform = info;
		return false;
	}
	
	/**
	 * @param usrName 用户名
	 * @param password 密码
	 * @return 注册是否成功
	 * 		   	需要填充的内容:
	 * 			inform:注册失败原因
	 * 			
	 */
	public static boolean register(String usrName,char[] password)
	{
		//TODO
		String info = client.signUp(usrName, new String(password));
		if(info.indexOf("_") != -1) {
			int type = Integer.parseInt(info.substring(0, info.indexOf("_")));
			if(type == 5) {
				inform = info.substring(info.indexOf("_")+1, info.length());
				return false;
			}
			else {
				inform = registerSuccess;
				return true;
			}
		}
		else 
			inform = info;
		return false;
	}
	

	public static ConcurrentHashMap<Integer, user> getFriendList(){
		return client.getFriendList();
	}
	
	public static ConcurrentHashMap<Integer, group> getGroupList(){
		return client.getGroupList();
	}
	
	public static class user
	{
		String name;
		int id;
		ImageIcon icon;
		public user(String name, int id, ImageIcon icon){
			this.name = name;
			this.id = id;
			this.icon = icon;
		}
		
		public String get_name() {
			return this.name;
		}
		public void set_name(String name) {
			this.name = name;
		}
		public ImageIcon get_icon() {
			return this.icon;
		}
		public void set_icon(ImageIcon icon) {
			this.icon = icon;
		}
		
		user(int i)//这个函数是测试用的，请自行添加字段和构造函数
		{
			//测试用代码
			name="崔牧原";
			icon=new ImageIcon("cmy.jpg");
		
		}
	}
	
	public static class group{
		String name;
		int id;
		int owner;
		ImageIcon profile;
		public group(String name, int id, int owner, ImageIcon profile){
			this.name = name;
			this.id = id;
			this.owner = owner;
			this.profile = profile;
		}
	}
	
	//以下为UI需实现的方法
	/**
	 * @param group_id: 群id
	 * @param friend_id: 发送消息的用户id
	 * @param is_group: 是否是群
	 * @param icon: 发从消息者的头像
	 * @param time: 发送消息的时间
	 * @param info: 发送的内容
	 */
	public static void showMessage(int group_id, int friend_id, boolean is_group, ImageIcon icon, String time, String info) {
		int chatid;
		String chatName;
		
		if(is_group)
		{
			chatid=group_id;
			
		}
	}
	
	public static void showFriendMessage(int id, String time, String info){ 
		Chat nowChat=new Chat(false,id,getUser(id).icon,getUser(id).name);
		JPanel aSendMsg=new JPanel(new GridBagLayout());
		//System.out.println(mainChat.getSize());
		//aSendMsg.setBackground(Color.GREEN);
		JTextArea msg=new JTextArea();
		String text=info;
		if (MainUI.midDown == null){
			System.out.println("HAHA");
		}
		MainUI.midDown.getChatInform(nowChat);
		MainUI.midDown.toTop(nowChat);
		
		ImageIcon i=new ImageIcon();
		i.setImage(nowChat.icon.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		JLabel userIcon=new JLabel(i);
		userIcon.setPreferredSize(new Dimension(50,50));
		userIcon.setMinimumSize(new Dimension(50,50));
		GridBagConstraints temp=MainUI.simpleCons(0, 0);
		temp.ipadx=30;
		temp.anchor=temp.NORTHWEST;
		aSendMsg.add(userIcon,temp);
		
		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		msg.setText(text);
		msg.setSize(new Dimension(350,10000));
		msg.setMinimumSize(new Dimension(350,50));
		msg.setFont(new Font("微软雅黑",Font.PLAIN,18));
		msg.setBackground(new Color(158,234,106));
		temp=MainUI.simpleCons(1, 0);
		temp.fill=temp.VERTICAL;
		//temp.ipadx=15;
		aSendMsg.add(msg,temp);
		
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		
		//System.out.println(MainUI.midDown.chatList.elementAt(MainUI.midDown.chatList.indexOf(nowChat)).mainChatNum);
		
		temp=MainUI.simpleCons(0, nowChat.mainChatNum++);
		temp.anchor=temp.NORTHWEST;
		temp.ipady=20;
		nowChat.mainChat.add(aSendMsg, temp);
		
		temp=MainUI.simpleCons(1, nowChat.mainChatNum-1);
		temp.weightx=100;
		nowChat.mainChat.add(Chat.blank,temp);	
	}
	
	public static void showFriendMessage(int id, String time, String info,ImageIcon icon,String name){ 
		Chat nowChat=new Chat(false,id,icon,name);
		JPanel aSendMsg=new JPanel(new GridBagLayout());
		//System.out.println(mainChat.getSize());
		//aSendMsg.setBackground(Color.GREEN);
		JTextArea msg=new JTextArea();
		String text=info;
		if (MainUI.midDown == null){
			System.out.println("HAHA");
		}
		//MainUI.midDown.getChatInform(nowChat);
		nowChat=MainUI.midDown.toTop(nowChat);
		
		ImageIcon i=new ImageIcon();
		i.setImage(nowChat.icon.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		JLabel userIcon=new JLabel(i);
		userIcon.setPreferredSize(new Dimension(50,50));
		userIcon.setMinimumSize(new Dimension(50,50));
		GridBagConstraints temp=MainUI.simpleCons(0, 0);
		temp.ipadx=30;
		temp.anchor=temp.NORTHWEST;
		aSendMsg.add(userIcon,temp);
		
		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		msg.setText(text);
		msg.setSize(new Dimension(350,10000));
		msg.setMinimumSize(new Dimension(350,50));
		msg.setFont(new Font("微软雅黑",Font.PLAIN,18));
		msg.setBackground(new Color(158,234,106));
		temp=MainUI.simpleCons(1, 0);
		temp.fill=temp.VERTICAL;
		//temp.ipadx=15;
		aSendMsg.add(msg,temp);
		
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		
		//System.out.println(MainUI.midDown.chatList.elementAt(MainUI.midDown.chatList.indexOf(nowChat)).mainChatNum);
		
		System.out.println("receive:"+nowChat.mainChatNum);
		temp=MainUI.simpleCons(0, nowChat.mainChatNum++);
		temp.anchor=temp.NORTHWEST;
		temp.ipady=20;
		nowChat.mainChat.add(aSendMsg, temp);
		
		temp=MainUI.simpleCons(1, nowChat.mainChatNum-1);
		temp.weightx=100;
		nowChat.mainChat.add(Chat.blank,temp);	
	}
	
	public static void showGroupMessage(int group_id, String speaker_name, ImageIcon speaker_profile, String time, String info){
		Chat nowChat=new Chat(true,group_id,getGroup(group_id).profile,getGroup(group_id).name);
		JPanel aSendMsg=new JPanel(new GridBagLayout());
		//System.out.println(mainChat.getSize());
		//aSendMsg.setBackground(Color.GREEN);
		JTextArea msg=new JTextArea();
		String text=info;
		if (MainUI.midDown == null){
			System.out.println("HAHA");
		}
		//MainUI.midDown.getChatInform(nowChat);
		nowChat=MainUI.midDown.toTop(nowChat);
		
		ImageIcon i=new ImageIcon();
		i.setImage(speaker_profile.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		JLabel userIcon=new JLabel(i);
		userIcon.setPreferredSize(new Dimension(50,50));
		userIcon.setMinimumSize(new Dimension(50,50));
		GridBagConstraints temp=MainUI.simpleCons(0, 0);
		temp.ipadx=30;
		temp.anchor=temp.NORTHWEST;
		aSendMsg.add(userIcon,temp);
		
		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		msg.setText(info);
		msg.setSize(new Dimension(350,10000));
		msg.setMinimumSize(new Dimension(350,50));
		msg.setFont(new Font("微软雅黑",Font.PLAIN,18));
		msg.setBackground(new Color(158,234,106));
		temp=MainUI.simpleCons(1, 0);
		temp.fill=temp.VERTICAL;
		//temp.ipadx=15;
		aSendMsg.add(msg,temp);
		
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		//System.out.println("mainChatNum:"+nowChat.mainChatNum);
		
		//System.out.println(MainUI.midDown.chatList.elementAt(MainUI.midDown.chatList.indexOf(nowChat)).mainChatNum);
		
		System.out.println("receive:"+nowChat.mainChatNum);
		temp=MainUI.simpleCons(0, nowChat.mainChatNum++);
		temp.anchor=temp.NORTHWEST;
		temp.ipady=20;
		nowChat.mainChat.add(aSendMsg, temp);
		
		temp=MainUI.simpleCons(1, nowChat.mainChatNum-1);
		temp.weightx=100;
		nowChat.mainChat.add(Chat.blank,temp);	
	}
	
	public static user getUser(int id){
		return client.getFriendList().get(id);
	}
	
	public static group getGroup(int id){
		return client.getGroupList().get(id);
	}
	
	/**
	 * @param self_id:发出添加好友请求者自己的id
	 */
	static void addNewFriend(int self_id) {
		//TODO 当用户决定添加好友时，为其显示该窗口，由ui端进行调用
		JTextArea input_name = new JTextArea();
		Object[] msg = {"请输入好友名称", input_name, "您需要输入验证消息，等待对方通过"};
		
		String result = JOptionPane.showInputDialog(null, msg);
		if(result != null) {
			//System.out.println(result + " " + input_name.getText());
			client.sendAddFriendRequest(self_id, input_name.getText(), result);
		}
	}
	
	/**
	 * @param new_friend:发出好友请求的用户名字
	 * @param info:验证信息
	 */
	public static void showAddFriendRequest(String new_friend, String info) {
		//TODO 将该用户的加好友请求进行显示，由client端进行调用
		String text = new_friend + ":" + info;
		int result = JOptionPane.showConfirmDialog(null, text, "好友请求", JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {
			client.sendAddFriendReply(new_friend, true);
		}
		else {
			client.sendAddFriendReply(new_friend, false);
		}
	}
	/**
	 * @param new_name:回复好友请求者的名字
	 * @param new_id:回复好友请求者的id
	 * @param new_profile:回复好友请求者的头像
	 * @param accept:对方是否同意了好友请求
	 */
	public static void showAddFriendReply(String new_name, int new_id, ImageIcon new_profile, boolean accept) {
		//TODO 将该用户的加好友请求的回复进行显示
		//TODO 如果请求被拒绝，则显示加好友失败
		//TODO 由client端调用
		String text = "";
		if(accept) {
			text = new_name + "成为新的好友";
			MainUI.fMidDown.friendList.add(new Friend(false,new_id,new_profile,new_name));
			MainUI.fMidDown.refresh();
			MainUI.switchToChat();
			Chat nowChat=new Chat(false,new_id,new_profile,new_name);
			nowChat=MainUI.midDown.toTop(nowChat);
			nowChat.toMainUI();
			
			
		}
		else 
			text = new_name + "拒绝了您的好友请求";
		JOptionPane.showMessageDialog(null, text, "好友请求回复", JOptionPane.DEFAULT_OPTION);
		
	}
}

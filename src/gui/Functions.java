package gui;

import testChat.Client;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

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
	static HashMap<Integer, HashMap<Integer, HashMap<Integer, String>>> replyList;
	
	public static final String registerSuccess = "成功注册！";
	public static final String signInSuccess = "成功登陆！";
		
	public static final String success = "发送成功！";
	public static final String timeOut = "发送超时！";
	public static final String notFriend = "对方不是您的好友！";
	public static final String notGroupMember = "您已不是该群的成员！";
	
	public static void setClient(Client new_client) {
		client = new_client;
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
	
<<<<<<< HEAD
	
	static class user
=======

	public static ConcurrentHashMap<Integer, user> getFriendList(){
		return client.getFriendList();
	}
	
	public static ConcurrentHashMap<Integer, group> getGroupList(){
		return client.getGroupList();
	}
	
	public static class user
>>>>>>> 622f5ef4460f2876d518e89dff501c6c3713e76f
	{
		String name;
		int id;
		ImageIcon icon;
		public user(String name, int id, ImageIcon icon){
			this.name = name;
			this.id = id;
			this.icon = icon;
		}
		user(int i)//这个函数是测试用的，请自行添加字段和构造函数
		{
			name="崔牧原";
			icon=new ImageIcon("cmy.jpg");
		}
		//TODO
	}
	
	public static class group{
		String name;
		int id;
		public group(String name, int id){
			this.name = name;
			this.id = id;
		}
	}
	
	//以下为UI需实现的方法
	/**
	 * @param profile 
	 * @param friend_name:发送信息者的用户名（可能是对方，也可能是自己）
	 * @param time:发从消息的时间,格式为year-month-date hour:minute:second
	 * @param info:发送的信息
	 */
	public static void showFriendMessage(String friend_name, ImageIcon profile, String time, String info) {
		//TODO 这个函数显示一对一聊天信息
		
	}
	/**
	 * @param group_name:显示消息的群名
	 * @param friend_name:显示消息的用户名（可能是对方，也可能是自己）
	 * @param time:发从消息的时间,格式为year-month-date hour:minute:second
	 * @param info:发送的消息
	 */
	public static void showGroupMessage(String group_name, String friend_name, String time, String info) {
		//TODO 这个函数显示群聊信息
		
	}
	/**
	 * @param new_friend:发出好友请求的用户名字
	 * @param info:验证信息
	 */
	public static void showAddFriendRequest(String new_friend, String info) {
		//TODO 将该用户的加好友请求进行显示
		
	}
	/**
	 * @param new_friend:回复好友请求的用户名字
	 * @param info:回复信息，“1”为加好友通过，“0”为加好友拒绝
	 */
	public static void showAddFriendReply(String new_friend, String info) {
		//TODO 将该用户的加好友请求的回复进行显示
		//TODO 如果请求被拒绝，则显示加好友失败
		//TODO 如果加好友成功，则将新加的好友的对话框显示在聊天列表的最上面，并将该新好友显示在好友列表中
		
	}
	/**
	 * @param chat_name:该聊天的名字（好友名字或群名）
	 * @param isgroup:是否为群聊，true为是，false为否
	 */
	public static void isNotFriend(String chat_name, boolean isgroup) {
		//TODO 当用户要在该聊天发送消息时，但对方好友已经将该用户删除/群主已经将该用户踢出群聊，
		//TODO 则在当前界面显示“对方不是您的好友”，或“您已被群主踢出群聊”等
		
	}
	public static void updateGroupList() {
		//TODO 对群列表进行更新，当被别人拉进一个群中，或被群主踢出群时进行调用
		//TODO 聊天列表中不会进行对新群的显示或旧群的删除，只是对群列表进行更新
	}

	public static void showGroupMessage(String group_name, String friend_name, ImageIcon profile, String time,
			String info) {
		// TODO Auto-generated method stub
		
	}
}

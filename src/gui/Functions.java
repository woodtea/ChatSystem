package gui;

import testChat.Client;

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
	
	static void setClient(Client new_client) {
		client = new_client;
	}
	
	static void signOff() {
		//TODO 通知后端结束进程
		client.signOff();
	}
	
	/**
	 * @param usrName 用户名
	 * @param password 密码
	 * @return 登录操作是否成功
	 * 			需要填充的内容:
	 * 			hostUser:用户		
	 * 		
	 */
	static boolean log(String usrName,char[] password)
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
				inform = "log in successfully.";
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
	static boolean register(String usrName,char[] password)
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
				inform = "register successfully.";
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
	static ConcurrentHashMap<Integer, user> getFriendList(){
		return client.getFriendList();
	}
	
	static ConcurrentHashMap<Integer, group> getGroupList(){
		return client.getGroupList();
	}
	
	public static class user
>>>>>>> origin/master
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
	 * @param friend_name:发送信息者的用户名（可能是对方，也可能是自己）
	 * @param time:发从消息的时间,格式为year-month-date hour:minute:second
	 * @param info:发送的信息
	 */
	public static void showFriendMessage(String friend_name, String time, String info) {
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
}

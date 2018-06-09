package testChat;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * @author z55250825
 * 所有的消息采用Message类序列化的方法传输
 * ①type：需要根据type类型来判断是消息种类(相关Type信息见末)
 * ②from: 根据from确定该消息来源于谁
 * ③to: 根据to确定该消息该发送给谁
 * ④msg：发送的消息
 * 
 * 在客户端层面打算只以名字作为区分
 */
public class Message implements Serializable, Cloneable {
	private String ServerId;
	private int type;
	private String from;
	private String to;
	private boolean isgroup;
	private String msg;

	Message() {ServerId="";type=0;from="";to="";msg="";isgroup=false;}

	Message(int type, String msg) {this.type = type;this.msg = msg;}

	Message(int type, String from, String to, boolean isgroup, String msg) {
		this.ServerId = "";
		this.type = type;
		this.from = from;
		this.to = to;
		this.isgroup = isgroup;
		this.msg = msg;
	}

	String get_id(){return ServerId;}
	void set_id(String ServerId){this.ServerId = ServerId;}
	
	int get_type() {return type;}

	String get_from() {return from;}

	String get_to() {return to;}

	boolean get_isgroup() {return isgroup;}

	String get_msg() {return msg;}
	
	void set_from(String from) {this.from = from;}
	void set_to(String to) {this.to= to;}
	void set_isgroup(boolean isgroup) {this.isgroup = isgroup;}
	void set_msg(String msg) {this.msg = msg;}
	
	public Message copy(){
		Message new_msg = new Message(type,from,to,isgroup,msg);
		new_msg.set_id(this.ServerId);
		return new_msg;
	}
	
	public String toString() {
		return "type:" + type + ", from:" + from + ", to:" + to + ", isgroup:" + isgroup + ", msg:" + msg;
	}
}

/*
 * Type数字与对应种类
 * 
 * 0: 心跳包
 * 1: 客户端注册包
 * 2: 客户端登录包 
 * 3: 客户端关闭包 
 * 4: 服务器控制信息包（成功登录类型) 
 * 5: 服务器控制信息包（登录注册错误（错误信息见msg）） 
 * 6: 加好友请求信息包 
 * 7: 加好友回复信息包
 * 8: 聊天信息包 
 * 9: 请求好友列表包
 * 10: 删除好友
 * 11: 创建群聊
 * 12: 解散群聊
 * 13: 邀请好友进群
 * 14: 退群
 * 15: 群主踢人
 * 16: 请求群列表包
 * 17: 确认帧(发送成功/发送失败及原因)
 */
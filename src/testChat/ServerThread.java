package testChat;

import gui.Functions;
import java.io.*;
import java.net.*;
import java.util.*;

class BroadCastThread extends Thread{
	Server mainServer = null;
	Vector<String> to_member;
	Message msg=null;
	
	public BroadCastThread(Server mainServer, Vector<String> to_member,Message msg){
		this.mainServer=mainServer;
		this.to_member=to_member;
		this.msg=msg;
	}
	
	public void run(){
		for (String to : to_member){
			ServerThread st=mainServer.get_accountServer(to);
			if (st!=null)
				st.write_to_account(msg);
			else{
				
			}
		}
	}
}

public class ServerThread extends Thread {
	private Server mainServer=null;
	private Socket socket=null;
	private String name=null;
	
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	private byte[] bob;
	
	//private ArrayBlockingQueue<Message> queue=new ArrayBlockingQueue<Message>(100,false);
	
	public ServerThread(){}
	
	public ServerThread(Server mainServer, Socket socket){
		this.mainServer=mainServer;
		this.socket=socket;
	}
	
	/*
	 * 向负责的account发送msg包
	 */
	public void write_to_account(Message msg){
		
		if(msg.get_type() == 8) {
			String info = SecurityCipher.get_send(bob, msg.get_msg());
			msg.set_msg(info);
		}
		
		synchronized(oos){
			IOControl.print(oos, msg);
		}
	}
	
	public boolean isFriend(Integer id1, Integer id2) {
		String tmp = mainServer.get_friend(id1);
		if(tmp != null) {
			String[] list = tmp.trim().split(" ");
			for(String i : list) {
				if(i.equals(id2.toString()))
					return true;
			}
		}
		return false;
	}
	
	public boolean isGroupMember(String user_id, String group_id) {
		Vector<String> member = mainServer.get_groupMember(group_id);
		if(member.contains(user_id))
			return true;
		else
			return false;
	}
	
	/*
	 * 先验证登录确认身份，或者注册新用户.
	 * 
	 * 然后将该用户相关的可联系用户与群信息发过去
	 * 
	 * 之后通过双线程的IO保证
	 */
	@Override
	public void run(){
		InputStream is=null;
		InputStreamReader isr=null;
		BufferedReader br=null;
		OutputStream os=null;
		PrintWriter pw=null;
		oos=null;
		ois=null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			os = socket.getOutputStream();
			pw = new PrintWriter(os);
			oos = new ObjectOutputStream(os);
			
			is=socket.getInputStream();
			isr=new InputStreamReader(is);
			br=new BufferedReader(isr);
			ois=new ObjectInputStream(is);
			
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			bob = SecurityCipher.GenerateKey_Server(dis, dos);
			
			boolean alreadySignIn=false;
			
			while (alreadySignIn==false)
			{
				Message msg=IOControl.read(ois);
				if (msg==null)
				{
					System.out.println("null class!");
					return;
				}
				
				int type=msg.get_type();
				String info=null;
				String[] tmp=null;
				if (type!=3)
				{
					info=msg.get_msg();
					
					info = SecurityCipher.get_receive(bob, info);
					
					tmp=info.split(" ");
				}
				
				if (type==1){
					String result=mainServer.sign_up(tmp[0], tmp[1]);
					if (result!=null)
						IOControl.print(oos,new Message(5,result));
					else
						IOControl.print(oos,new Message(5,"signupsuccess!"));
				}
				
				if (type==2){
					String result=mainServer.sign_in(tmp[0], tmp[1], this);
					if (result!=null)
						IOControl.print(oos,new Message(5,result));
					else
					{
						alreadySignIn=true;
						name=tmp[0];
						IOControl.print(oos,new Message(4,
							mainServer.get_account_message(tmp[0])));
					}
				}
				
				if (type==3)return;
			}
			
			/*
			 * 以下是sign in的情况
			 * 几种操作：
			 * ①添加好友，为ADD 好友名 信息
			 * ②向好友发送信息，为 CHAT 好友名 信息
			 * （目前测试直接在server加入好友信息
			 * 
			 * 但是因为需要同时处理收发,所以需要继续分化进程
			 * 目前未做，因为没有？？
			 */
			
			boolean alreadySignOff=false;
			
			while (alreadySignOff==false){
				Message msg=IOControl.read(ois);
				
				if (msg==null)
				{
					System.out.println("Null Class");
					return;
				}
				int type=msg.get_type();
				String from=msg.get_from();
				String to=msg.get_to();
				boolean isgroup=msg.get_isgroup();
				String info=msg.get_msg();
				
				Vector<String>to_member=new Vector<String>();
				BroadCastThread broad=null;
				
				/*
				 * 加好友请求
				 * type==6
				 * from:发出好友请求的用户id
				 * to:请求的好友的name
				 */
				if (type==6){
					String to_id;
					if(mainServer.get_name2id(to) != null) {
						//请求的好友存在，则得到其id，将好友请求发送给他
						to_id = mainServer.get_name2id(to).toString();
						to_member.addElement(to_id);
						
						//需要显示
						msg.set_from(mainServer.get_id2name(from));
						
						broad = new BroadCastThread(mainServer, to_member, msg);
						broad.start();
					}
					else {
						//请求的好友不存在，直接给from发送加好友失败信息
						to_member.addElement(from);
						Message new_msg = new Message(18,"",from,false,"请求的好友不存在！");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
				}
				/*
				 * 好友请求回复
				 * type==7
				 * msg: 1:同意好友请求/0:好友请求被拒绝 + "_" + from + 
				 */
				if (type==7){
					if (info.equals("1")) {
						mainServer.add_friend(Integer.parseInt(to), Integer.parseInt(from));
						to_member.addElement(from);
					}
					to_member.addElement(to);
					broad = new BroadCastThread(mainServer, to_member, msg);
					broad.start();
				}
				/*
				 * 聊天信息包
				 * type==8
				 * from 信息来自的用户
				 * to 信息去向的用户/群
				 * msg 聊天内容
				 */
				if (type==8){
					Message new_msg=null;
					
					//type == 8需要解密
					info = SecurityCipher.get_receive(bob, info);
					
					// 发送信息者的头像
					String messageNumber = info.substring(0, info.indexOf("_"));
					info = info.substring(info.indexOf("_") + 1, info.length());
					if(isgroup) {
						if(isGroupMember(from, to)) {
							//发消息者在群中
							to_member = mainServer.get_groupMember(to);
							to_member.removeElement(from);
							//msg : 信息发送者 + 信息发送的群 + 发送者的头像
							new_msg = new Message(type, from+"_"+to, to, isgroup, info);
							broad = new BroadCastThread(mainServer, to_member, new_msg);
							broad.start();
							
							Vector<String> reply_member = new Vector<String>();
							reply_member.addElement(from);
							Message reply_msg = new Message(17, from, to, false, messageNumber + "_" + Functions.success);
							BroadCastThread reply_broad = new BroadCastThread(mainServer, reply_member, reply_msg);
							reply_broad.start();
						}
						else {
							//发消息者不在群中，发送失败
							Vector<String> reply_member = new Vector<String>();
							reply_member.addElement(from);
							Message reply_msg = new Message(17, from, to, false, 
									messageNumber + "_" + Functions.notGroupMember);
							BroadCastThread reply_broad = new BroadCastThread(mainServer, reply_member, reply_msg);
							reply_broad.start();
						}
					}
					else{
						if(isFriend(Integer.parseInt(from), Integer.parseInt(to))) {
							//两人是好友
							to_member.addElement(to);
							//msg : 信息发送者 + 发送者的头像
							new_msg= new Message(type, from, to, isgroup, info);
							broad = new BroadCastThread(mainServer, to_member, new_msg);
							broad.start();
							
							Vector<String> reply_member = new Vector<String>();
							reply_member.addElement(from);
							Message reply_msg = new Message(17, from, to, false, messageNumber + "_" + Functions.success);
							BroadCastThread reply_broad = new BroadCastThread(mainServer, reply_member, reply_msg);
							reply_broad.start();
						}
						else {
							//两人不是好友，发送失败
							Vector<String> reply_member = new Vector<String>();
							reply_member.addElement(from);
							Message reply_msg = new Message(17, from, to, false, messageNumber + "_" + Functions.notFriend);
							BroadCastThread reply_broad = new BroadCastThread(mainServer, reply_member, reply_msg);
							reply_broad.start();
						}
					}
				}
				/*
				 * 请求好友列表
				 * type==9
				 * msg: name + " " + id + "<profile>" + icon + "</profile>"	
				 */
				if (type==9){
					int id=Integer.parseInt(from);
					String tmp=mainServer.get_friend(id),result=id+"";
					if (tmp != null)
						result+=tmp;
					
					String fri[] = result.split(" ");
					String ans = "";
					for(String f : fri) {
						ans += mainServer.get_id2name(f) + " " + f + "<profile>" + mainServer.get_image(to) + "</profile>";
					}
					
					Message new_msg=new Message(9,"",from,false,ans);
					to_member.addElement(from);
					broad = new BroadCastThread(mainServer, to_member, new_msg);
					broad.start();
				}
				/*
				 * 删除好友
				 * type==10
				 */
				if (type == 10) {
					mainServer.delete_friend(Integer.parseInt(from), Integer.parseInt(to));
				}
				/*
				 * 创建群聊
				 * type==11
				 * from 群主
				 * to 群成员1+群成员2+……(包含群主)
				 * msg:群名称+"_"+新群id
				 */
				if(type == 11) {
					String tmp[] = to.split("_");
					for(String t : tmp) {
						to_member.addElement(t);
					}
					//调用mainServer的创建群聊的方法,得到新创建的群的id
					Integer new_group_id = mainServer.create_group(from, tmp, info);
					Message new_msg=new Message(11,from,to,false,info+"_"+new_group_id.toString());
					//告知群成员，更新群列表
					broad = new BroadCastThread(mainServer, to_member, new_msg);
					broad.start();
				}
				/*
				 * 解散群聊
				 * type==12
				 * from 解散群聊的用户,必须是群主
				 * to 解散的群聊id
				 */
				if(type == 12) {
					//告知群成员，更新群列表
					to_member = mainServer.get_groupMember(to);
					//调用mainServer的解散群聊的方法
					int delete_result = mainServer.delete_group(to);
					if(delete_result == 0) {
						broad = new BroadCastThread(mainServer, to_member, msg);
						broad.start();
					}
					else {
						to_member = new Vector<String>();
						to_member.addElement(from);
						Message new_msg = new Message(17,"",from,false,"删除群聊失败！");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
				}
				/*
				 * 邀请好友进群
				 * type==13
				 * from 发出邀请的用户
				 * to 邀请的用户+邀请其加入的群id
				 */
				if(type == 13) {
					//调用mainServer的向群聊中加成员的方法
					int insert_result = mainServer.insert_group_member(to.split("_")[0], to.split("_")[1]);
					if(insert_result == 0) {
						to_member.addElement(to.split("_")[0]);
						Message new_msg = new Message(type, from, to, false, "");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
					else {
						to_member.addElement(from);
						Message new_msg = new Message(17,"",from,false,"拉好友进群失败！");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
				}
				/*
				 * 退群
				 * type==14
				 * from 退群的用户
				 * to 退出的群id
				 */
				if(type == 14) {
					//调用mainServer的从群中去除某成员的方法
					int insert_result = mainServer.delete_group_member(from, to);
					if(insert_result == 0) {
						to_member.addElement(from);
						Message new_msg = new Message(type, from, to, false, "");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
					/*
					else {
						to_member.addElement(from);
						Message new_msg = new Message(17,"",from,false,"退群失败！");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
					*/
				}
				/*
				 * 踢人
				 * type==15
				 * from 群主
				 * to 移除的用户+移除的群id
				 */
				if(type == 15) {
					//调用mainServer的从群中去除某成员的方法
					int insert_result = mainServer.delete_group_member(to.split("_")[0], to.split("_")[1]);
					if(insert_result == 0) {
						to_member.addElement(to.split("_")[0]);
						Message new_msg = new Message(type, from, to, false, "");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
					else {
						to_member.addElement(from);
						Message new_msg = new Message(18,"",from,false,"移除群成员失败！");
						broad = new BroadCastThread(mainServer, to_member, new_msg);
						broad.start();
					}
				}
				/*
				 * 请求群列表
				 * type==16
				 * msg: name + " " + id + " " + owner + "\n"
				 */
				if(type == 16) {
					//调用mainServer的get_group方法得到群列表 
					int id=Integer.parseInt(from);
					String tmp=mainServer.get_group(id),result = "";
					if(tmp != null)
						result += tmp;
					
					String gru[] = result.split(" ");
					String ans = "";
					for(String g : gru) {
						ans += mainServer.get_group(Integer.parseInt(g)) + " " + g + " " + 
								mainServer.get_group_owner(g) + "\n";
					}
					
					Message new_msg=new Message(16,"",from,false,ans);
					to_member.addElement(from);
					broad = new BroadCastThread(mainServer, to_member, new_msg);
					broad.start();
				}
				if(type == 17) {
					//理论上服务器不会收到来自客户端的type==17的信息包
				}
				/*
				 * 用户登出
				 */
				if (type==3){
					alreadySignOff=true;
				}
			}
			
			sleep(10*1000);
		}catch(IOException e){
			System.out.println("IOException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			try{
				if (oos!=null)
					oos.close();
				if (ois!=null)
					ois.close();
				if (pw!=null)
					pw.close();
				if (os!=null)
					os.close();
				if (br!=null)
					br.close();
				if (isr!=null)
					isr.close();
				if (is!=null)
					is.close();
				if (socket!=null)
					socket.close();
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				System.out.println("socket end!");
				mainServer.delete_accountServer(this.name);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

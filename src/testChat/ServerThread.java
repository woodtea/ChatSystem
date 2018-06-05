package testChat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

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
		synchronized(oos){
			IOControl.print(oos, msg);
		}
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
		try {
			os = socket.getOutputStream();
			pw = new PrintWriter(os);
			oos = new ObjectOutputStream(os);
			
			is=socket.getInputStream();
			isr=new InputStreamReader(is);
			br=new BufferedReader(isr);
			ois=new ObjectInputStream(is);
			
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
				 */
				if (type==6){
					to_member.addElement(to);
					broad = new BroadCastThread(mainServer, to_member, msg);
					broad.start();
				}
				/*
				 * 好友请求回复
				 * type==7
				 * msg 1:同意好友请求, 0:拒绝好友请求
				 */
				if (type==7){
					if (info.equals("1")) {
						mainServer.add_friend(Integer.parseInt(to), Integer.parseInt(from));
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
					if(isgroup) {
						to_member = mainServer.get_groupMember(to);
						to_member.removeElement(from);
						new_msg = new Message(type, from+"_"+to, to, isgroup, info);
					}
					else{
						to_member.addElement(to);
						new_msg=msg;
					}
					broad = new BroadCastThread(mainServer, to_member, new_msg);
					broad.start();
				}
				/*
				 * 请求好友列表
				 * type==9
				 */
				if (type==9){
					int id=Integer.parseInt(from);
					String tmp=mainServer.get_friend(id),result=id+"";
					if (tmp!=null)
						result+=tmp;
					Message new_msg=new Message(9,"",from,false,result);
					to_member.addElement(from);
					broad = new BroadCastThread(mainServer, to_member, new_msg);
					broad.start();
				}
				/*
				 * 删除好友
				 * type==10
				 */
				if (type == 10) {
					mainServer.delete_accountServer(Integer.parseInt(to));
				}
				/*
				 * 创建群聊
				 * type==11
				 * from 群主
				 * to 群成员1+群成员2+……(不包含群主)
				 * msg:群名称
				 */
				if(type == 11) {
					String tmp[] = to.split("_");
					for(String t : tmp) {
						to_member.addElement(t);
					}
					//调用mainServer的创建群聊的方法
					
					
					broad = new BroadCastThread(mainServer, to_member, msg);
					broad.start();
				}
				/*
				 * 解散群聊
				 * type==12
				 * from 解散群聊的用户,必须是群主
				 * to 解散的群聊
				 */
				if(type == 12) {
					//调用mainServer的解散群聊的方法
				}
				/*
				 * 邀请好友进群
				 * type==13
				 * from 发出邀请的用户
				 * to 邀请的对象+邀请其加入的群
				 */
				if(type == 13) {
					//调用mainServer的向群聊中加成员的方法
				}
				if(type == 14) {
					//调用mainServer的从群中去除某成员的方法
				}
				if(type == 15) {
					//调用mainServer的从群中去除某成员的方法
				}
				if(type == 16) {
					//调用mainServer的get_group方法得到群列表 
				}
				if(type == 17) {
					
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
				if (oos!=null)oos.close();
				if (ois!=null)ois.close();
				if (pw!=null)pw.close();
				if (os!=null)os.close();
				if (br!=null)br.close();
				if (isr!=null)isr.close();
				if (is!=null)is.close();
				if (socket!=null)socket.close();
				System.out.println("socket end!");
				mainServer.delete_accountServer(this.name);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

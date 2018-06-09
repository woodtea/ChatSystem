package testChat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.awt.image.*;
import java.util.Base64.Decoder;

import javax.imageio.ImageIO; 

import java.sql.*;
import com.mchange.v2.c3p0.*;

/*
 * 想法：
 * 		①我觉得对于好友与群信息可以直接让客户端来做。
 * 		最初肯定客户端知道自己账号的好友信息与群信息，因此
 * 		完全不需要交给服务器来判断.直接让其来转发即可.
 * 		server更像一个路由的功能.
 * 		②数据库的事务回滚
 * 		③ACK帧确认重发.
 * 		④加密
 * 		⑤邮箱修改？
 */

/*
 * 新加:
 * ConcurrentHashMap account2id
 * ConcurrentHashMap id2account
 * 删除:
 * OnlineAccount
 */

/*
 * ToDoList:
 * ①邮箱修改密码
 * ②加密措施:已经完成
 * ③ACK与离线推送
 * ④数据库重复
 */

/*
 * default encoding format:utf-8
 * 
 * 默认命名规则:
 *		1)方法名采用 _ 分隔符命名
 *		2)类名,变量名采用大写命名.
 */

class SendIdentifier{
	String start;
	SendIdentifier(){start = "0";}
	SendIdentifier(String start){this.start = start;}
	String plus(String id){
		int len = id.length();
		String ans = "";
		boolean addOne = true;
		for (int i = len-1; i>=0; --i){
			if (addOne){
				if (id.charAt(i)=='9'){
					ans = "0" + ans;
				}
				else{
					ans = (char)(id.charAt(i)+1) + ans;
					addOne = false;
				}
			}
			else{
				ans = id.charAt(i) + ans;
			}
		}
		if (addOne){
			ans = "1" + ans;
		}
		return ans;
	}
	
	String getID(){
		String id = null;
		synchronized(start){
			id = start;
			start = plus(start);
		}
		return id;
	}
}

public class Server {
	ConcurrentHashMap<String, String> account;
	volatile ConcurrentHashMap<String, ServerThread> accountServer;
	ConcurrentHashMap<String, Vector<String>> groupMember;
	ConcurrentHashMap<String, String> group2owner;
	ConcurrentHashMap<String, Integer> account2id;
	ConcurrentHashMap<Integer, String> id2account;

	private final static String separator = System.getProperty("file.separator");
	private final static String line = System.lineSeparator();

	Integer accountNo = 0;
	Integer groupNo = 0;
	
	SendIdentifier si;
	volatile private boolean canUpload = true; 

	/*
	 * 从服务器本地指定的文件导入指定的账号名 与密码(每行输入格式为: 账号名 密码 ) 初始化其他数组
	 *
	 * 默认Server初始化从load_Account开始
	 */
	private void load_Account() {
		account = new ConcurrentHashMap<String, String>();
		accountServer = new ConcurrentHashMap<String, ServerThread>();
		groupMember = new ConcurrentHashMap<String, Vector<String>>();
		group2owner = new ConcurrentHashMap<String,String>();
		account2id = new ConcurrentHashMap<String, Integer>();
		id2account = new ConcurrentHashMap<Integer, String>();

		account.clear();
		accountServer.clear();
		groupMember.clear();
		group2owner.clear();
		account2id.clear();
		id2account.clear();

		account.put("server", "");

		DBControl.initialize();

		DBControl db = new DBControl(true);

		String sql = "select * from account";
		db.getStatement(sql);
		ResultSet rs = db.query();

		try {
			if (rs != null) {
				while (rs.next()) {
					int id = rs.getInt("id");
					String name = rs.getString("name");
					String passwd = rs.getString("password");
					account.put(name, passwd);
					account2id.put(name, id);
					id2account.put(id, name);
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		accountNo = account.size() - 1;

		db.closeResultSet(rs);
		db.clean();
		
		db = new DBControl(true);
		
		sql = "select MAX(group_id) as a from groups";
		db.getStatement(sql);
		rs = db.query();
		
		try {
			if (rs == null)
				groupNo = 0;
			else {
				while (rs.next()) {
					groupNo = rs.getInt("a");
				}
			}
		}catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		db.closeResultSet(rs);
		db.clean();
		
		db = new DBControl(true);
		
		sql = "select distinct * from group_member";
		db.getStatement(sql);
		rs = db.query();
		
		try {
			if (rs != null)
				while (rs.next()) {
					String groupID = ""+rs.getInt("group_id");
					String memberID = ""+rs.getInt("member_id");
					if (groupMember.containsKey(groupID))
						groupMember.get(groupID).addElement(memberID);
					else
					{
						Vector<String> tmp = new Vector<String>();
						tmp.addElement(memberID);
						groupMember.put(groupID,tmp);
					}
				}
		}catch (SQLException e){
			e.printStackTrace();
		}
		db.closeResultSet(rs);
		db.clean();
		
		db = new DBControl(true);
		sql = "select group_id,owner_id from groups";
		db.getStatement(sql);
		rs = db.query();
		
		try {
			if (rs!=null)
				while (rs.next()) {
					String groupID = ""+rs.getInt("group_id");
					String ownerID = ""+rs.getInt("owner_id");
					group2owner.put(groupID, ownerID);
				}
		} catch (SQLException e){
			e.printStackTrace();
		}
		db.closeResultSet(rs);
		db.clean();
		
		File filepath = new File("msg_id.txt");
		BufferedReader reader = null;
		if (filepath.exists()){
			String now_si = null;
			try {
				reader = new BufferedReader(new FileReader(filepath));
				now_si = reader.readLine();
				si = new SendIdentifier(now_si);
				si.getID();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null)
					try{
						reader.close();
					} catch (IOException e){
						e.printStackTrace();
					}
			}
		}else{
			si = new SendIdentifier();
		}
		printState();
	}

	/*
	 * 注册用户名与对应密码，以下情况无法通过： 1)名字已经被注册. 2)密码太简单(这个由客户端判断)
	 * 
	 * 如果出错，返回值为出错详细信息，否则为null
	 * 
	 */
	protected String sign_up(String name, String passwd) {
		if (account.containsKey(name) == true)
			return "The account name already exists!";

		String newPasswd = SecurityCipher.getSHA256Str(passwd);
		int newAccountNo = 0;
		synchronized (accountNo) {
			accountNo += 1;
			newAccountNo = accountNo;
		}

		account.put(name, newPasswd);
		account2id.put(name, newAccountNo);
		id2account.put(newAccountNo, name);

		DBControl db = new DBControl(true);

		String sql = "INSERT INTO account(id,name,password) VALUE(?,?,?)";
		PreparedStatement stmt = db.getStatement(sql);

		try {
			stmt.setInt(1, newAccountNo);
			stmt.setString(2, name);
			stmt.setString(3, newPasswd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;

		db.update();

		db.clean();
		return null;
	}

	/*
	 * 登录用户名与密码，以下情况无法通过： 1）名字不存在 2）密码错误 3）已经登录
	 * 对于登录成功的用户名，用accountServer记录其对应Server线程.
	 * 
	 */
	protected String sign_in(String name, String passwd, ServerThread server) {
		String newPasswd = SecurityCipher.getSHA256Str(passwd);
		
		if (account.containsKey(name) == false)
			return "The account name doesn't exist!";
		if (accountServer.containsKey(name) == true)
			return "The account already sign in!";
		if (account.get(name).equals(newPasswd) == false)
			return "wrong password!";
		accountServer.put(name, server);
		return null;
	}

	/*
	 * 每个账号需要记录的信息: ①头像(待update),姓名 ②所有好友与已经加入的群名 ③未接收的信息
	 * 
	 */
	protected String get_account_message(String name) {
		String result = "", tmp = null;
		int id = account2id.get(name);

		tmp = get_friend(id);
		if (tmp != null) {
			result += tmp;
			tmp = null;
		}

		String result2 = "";
		
		tmp = get_group(id);
		if (tmp != null) {
			result2 += tmp;
			tmp = null;
		}
		return id+result+"\n"+result2;
	}
	
	/*
	 * 返回群信息
	 * 
	 */
	protected String get_group(int id){
		String result = null;
		DBControl db = new DBControl(true);
		
		String sql = "select a.group_id as id, b.group_name as name "
				+"from group_member as a,groups as b where a.member_id=? and a.group_id=b.group_id";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setInt(1, id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;
		
		ResultSet rs = db.query();
		try {
			if (rs != null){
				result = "";
				boolean start = true;
				while (rs.next()) {
					int groupID = rs.getInt("id");
					String name = rs.getString("name");
					if (!start)
						result += " ";
					result += (groupID + " " + name);
					start = false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		db.closeResultSet(rs);
		db.clean();
		//System.out.println("Result "+result);
		return result;
	}

	protected String get_friend(int id) {
		String result = null;
		DBControl db = new DBControl(true);

		String sql = "select distinct b.friend_id as id,c.name as name from account as a,friend as b,"
				+ "account as c where a.id=? and a.id=b.id and b.friend_id=c.id";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setInt(1, id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;

		ResultSet rs = db.query();
		try {
			if (rs != null) {
				result = "";
				while (rs.next()) {
					int fid = rs.getInt("id");
					String name = rs.getString("name");
					// 要求名字里没有\n
					result += (" " + fid + " " + name);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		db.closeResultSet(rs);
		db.clean();
		return result;
	}

	/*
	 * 修改密码，这个应该是在登录之后允许的操作. 对于忘记密码没有特别方便的技巧，除非允许邮箱找回(以后)
	 * 
	 * 引入三个参数，用户名，老密码，新密码
	 * 
	 * 如果出错，返回值为详细出错信息，否则为null
	 * 
	 * 同时修改密码的同时要修改本地服务器存储的用户-密码文件 保证本地的密码库正确
	 */
	protected String modify_passwd(String name, String oldpasswd, String newpasswd) {
		if (account.containsKey(name) == false)
			return "The account name doesn't exists!";
		String old = SecurityCipher.getSHA256Str(oldpasswd);
		String newp = SecurityCipher.getSHA256Str(newpasswd);
		
		if (account.get(name).equals(old) == false)
			return "Wrong old password!";
		account.put(name, newp);

		DBControl db = new DBControl(true);
		String sql = "UPDATE accout SET password=? where name=? and password=?";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setString(1, newp);
			stmt.setString(2, name);
			stmt.setString(3, old);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;
		db.update();

		db.clean();
		return null;
	}

	/*
	 * 关闭数据库连接
	 */
	protected void closeDB() {
		DBControl.close();
	}

	/*
	 * 查找是否存在某个名字的账号
	 */
	protected boolean check_exist(String name) {
		return account.containsKey(name);
	}

	protected boolean check_exist(int id) {
		return id2account.containsKey(id);
	}

	/*
	 * 检查某个账号是否在线（多态）
	 */
	protected boolean check_online(String name) {
		return accountServer.containsKey(name);
	}

	protected boolean check_online(int id) {
		String name = id2account.get(id);
		if (name == null)
			return false;
		return accountServer.containsKey(name);
	}

	/*
	 * 添加好友: 加好友,这里的好友流程如下: A向负责自己的服务器发送对应添加好友的Message类. to填的就是想要添加好友的用户B
	 * 服务器直接转发到A对应的服务器,然后转发给B的对应服务器. 然后B需要发送向自己的服务器发送同意包之后，然后B对应的
	 * 服务器通知中心服务器添加好友对.(对称) 之后转发给对方，双方获取新的好友列表.
	 * 
	 * 默认以id开头,且两个id不相等(由客户端判断) 如果id不存在的话是直接退出的
	 */
	protected void add_friend(int id, int friend_id) {
		/*
		 * 可能存在这样的情况，互相双方同时同意加对方，由于没有对id和friend_id同步，
		 * 因此可能会出现加两次的情况.(但由于SQL上的唯一性，第二次插入会抛出SQLException， 但对逻辑应该没有问题）
		 * 
		 * 客户端要处理同意包，注意如果已经好友列存在该好友了则无视该包.
		 */
		if (id2account.containsKey(id) == false || id2account.containsKey(friend_id) == false)
			return;
		DBControl db = new DBControl(true);

		String sql = "INSERT INTO friend(id,friend_id) VALUES (?,?), (?,?)";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, friend_id);
			stmt.setInt(3, friend_id);
			stmt.setInt(4, id);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.clean();
		}
	}

	/*
	 * 删除好友：删除好友应该是对称的 A向服务器发出删除好友B的Message,那么服务器会接收
	 * 该Message并让负责B的服务器向B发送删除Message. (这里的先后顺序似乎值得考虑)
	 * 
	 * 同样默认以id开头，且两个id不相等（由客户端判断） 并且两个id已经是好友了. 如果id不存在的话直接退出.
	 */
	protected void delete_friend(int id, int friend_id) {
		/*
		 * 同添加好友,可能存在这样的情况，互相双方同时删除对面，这样子的话 SQL会删除两次，同样会抛出Exception
		 * 删除同样客户端也要确认是否已经不在好友列.
		 * 
		 */
		if (id2account.containsKey(id) == false || id2account.containsKey(friend_id) == false)
			return;
		DBControl db = new DBControl(true);

		String sql = "DELETE FROM friend where (id=? and friend_id=?) or (id=? and friend_id=?)";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, friend_id);
			stmt.setInt(3, friend_id);
			stmt.setInt(4, id);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.clean();
		}
	}

	protected Vector<String> get_groupMember(String groupID) {
		if (groupMember.containsKey(groupID)) {
			Vector<String> groupList = groupMember.get(groupID);
			Vector<String> answer = null;
			synchronized(groupList){
				answer = (Vector<String>)groupList.clone();
			}
			return answer;
		}
		return null;
	}

	/*
	 * 返回对应accountID的server进程类.
	 */
	protected ServerThread get_accountServer(String accountID) {
		Integer id=Integer.parseInt(accountID);
		String name=id2account.get(id);
		
		if (accountServer.containsKey(name)) {
			return accountServer.get(name);
		}
		return null;
	}
	
	/*
	 * 从accountServer中删除某用户,默认用的是名字？
	 */
	protected void delete_accountServer(int id){
		String name=id2account.get(id);
		delete_accountServer(name);
	}
	
	protected void delete_accountServer(String name){
		System.out.println("account "+name+" log off");
		accountServer.remove(name);
	}
	
	/*
	 * 创建群聊
	 */
	protected int create_group(String from, String[] to, String groupName){
		DBControl db = new DBControl(true);
		
		String sql = "INSERT INTO groups(group_id,group_name,owner_id) VALUES(?,?,?)";
		PreparedStatement stmt = db.getStatement(sql);
		
		Integer fromId = Integer.parseInt(from);
		int newGroupId=0;
		
		synchronized(groupNo){
			groupNo += 1;
			newGroupId = groupNo;
		}
		
		try {
			stmt.setInt(1, newGroupId);
			stmt.setString(2, groupName);
			stmt.setInt(3, fromId);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.clean();
			db = null;
		}
		
		//接下来更新群成员
		db = new DBControl(true);
		
		sql = "INSERT INTO group_member(group_id,member_id) VALUES";
		
		boolean start = true;
		for (String member : to){
			if (member=="")continue;
			if (!start) sql += ",";
			sql += "(?,?)";
			start = false;
		}
		
		stmt = db.getStatement(sql);
		
		int nowPosition = 1;
		try {
			for (String member : to){
				if (member=="")continue;
				Integer memberId = Integer.parseInt(member);
				stmt.setInt(nowPosition, newGroupId);
				nowPosition += 1;
				stmt.setInt(nowPosition, memberId);
				nowPosition += 1;
			}
			stmt = null;
			db.update();
		}catch (SQLException e) {
			e.printStackTrace();
			db.clean();
			return -1;
		}finally{
			db.clean();
		}
		
		Vector<String> groupList = new Vector<String>();
		synchronized(groupList){
			groupMember.put(""+newGroupId, groupList);
			for (String member : to){
				if (member=="")continue;
				groupList.addElement(member);
			}
		}
		
		group2owner.put(""+newGroupId, from);
		
		return newGroupId;
	}
	
	protected int delete_group(String groupId){
		Vector<String> groupList = groupMember.get(groupId);
		if (groupList == null)
			return -1;
		
		DBControl db = new DBControl(true);
		
		Integer id = Integer.parseInt(groupId);
		
		String sql = "delete from group_member where group_id=?";
		PreparedStatement stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
			db.clean();
			return -1;
		}finally{
			db.clean();
			db = null;
		}
		
		db = new DBControl(true);
		sql = "delete from groups where group_id=?";
		stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
			db.clean();
			return -1;
		} finally {
			db.clean();
		}
		
		synchronized(groupList){
			groupList.clear();
			groupMember.remove(groupId);
		}
		
		group2owner.remove(groupId);
		
		return 0;
	}
	
	/*
	 * return 0 表示插入成功
	 * return 1 表示已经存在好友
	 * return -1 表示发生异常错误插入失败
	 */
	protected int insert_group_member(String to, String groupID){
		DBControl db = new DBControl(true);
		
		Integer id = Integer.parseInt(groupID);
		Integer toID = Integer.parseInt(to);
		
		String sql="select * from group_member where group_id=? and member_id=?";
		PreparedStatement stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, toID);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		stmt = null;
		
		ResultSet rs=db.query();
		try {
			if (rs!=null&&rs.next())
				return 1;
		} catch (SQLException e1) {
			e1.printStackTrace();
			db.closeResultSet(rs);
			db.clean();
			return -1;
		}
		
		db.closeResultSet(rs);
		db.clean();
		
		db = new DBControl(true);
		
		sql = "insert into group_member VALUES(?,?)";
		stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, toID);
		} catch (SQLException e) {
			e.printStackTrace();
			db.clean();
			return -1;
		}
		
		stmt = null;
		db.update();
		db.clean();
		
		Vector<String> groupList = groupMember.get(groupID);
		synchronized(groupList){
			groupList.addElement(to);
		}
		
		return 0;
	}
	
	protected int delete_group_member(String to, String groupID){
		DBControl db = new DBControl(true);
		
		Integer id = Integer.parseInt(groupID);
		Integer toID = Integer.parseInt(to);
		
		String sql="select * from group_member where group_id=? and member_id=?";
		PreparedStatement stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, toID);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		stmt = null;
		
		ResultSet rs=db.query();
		try {
			if (rs==null||rs.next()==false)
				return 1;
		} catch (SQLException e1) {
			e1.printStackTrace();
			db.closeResultSet(rs);
			db.clean();
			return -1;
		}
		
		db.closeResultSet(rs);
		db.clean();
		
		db = new DBControl(true);
		
		sql = "delete from group_member where group_id=? and member_id=?";
		stmt = db.getStatement(sql);
		
		try {
			stmt.setInt(1, id);
			stmt.setInt(2, toID);
		} catch (SQLException e) {
			e.printStackTrace();
			db.clean();
			return -1;
		}
		
		stmt = null;
		db.update();
		db.clean();
		
		Vector<String> groupList = groupMember.get(groupID);
		synchronized(groupList){
			groupList.remove(to);
		}
		
		return 0;
	}
	
	protected void set_image(String id, String base64String){
		BufferedImage tmp = ImageControl.base64StringToImg(base64String);
		
		File filePath = new File("img");
		if (!filePath.exists())
			filePath.mkdir();
		
		try {
			ImageIO.write(tmp, "jpg", new File("img"+separator+id+".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 返回null表示读取失败
	 */
	protected String get_image(String id){
		BufferedImage tmp = null;
		File filepath = new File("img"+separator+id+".jpg");
		String destination = "default";
		if (filepath.exists())
			destination = id+"";
		try {
			tmp = ImageIO.read(new FileInputStream("img"+separator+destination+".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return ImageControl.imgToBase64String(tmp);
	}
	
	protected Integer get_name2id(String name){
		return account2id.get(name);
	}
	
	protected String get_id2name(Integer id){
		return id2account.get(id);
	}
	
	protected String get_id2name(String id){
		Integer ID = Integer.parseInt(id);
		return id2account.get(ID);
	}
	
	void printState(){
		System.out.println();
		System.out.println("*************************");
		System.out.println("accountServer");
		for (String accountID : accountServer.keySet())
			System.out.println(accountID);
		
		System.out.println("account2id");
		for (String account : account2id.keySet()){
			Integer id = account2id.get(account);
			System.out.println(account + " " + id);
		}
		
		System.out.println("groupMember");
		for (String groupID : groupMember.keySet()){
			Vector<String> groupList = groupMember.get(groupID);
			System.out.print(groupID+" : ");
			for (String member : groupList){
				System.out.print(member+" ");
			}
			System.out.println();
		}
		
		System.out.println("group2owner");
		for (String groupID : group2owner.keySet()){
			String ownerID = group2owner.get(groupID);
			System.out.println(groupID + " " + ownerID);
		}
	}
	/*
	 * 判断某人是否是群群主
	 */
	protected boolean is_group_owner(String groupID, String ownerID){
		String owner = group2owner.get(groupID);
		if (owner == null)
			return false;
		return owner.equals(ownerID);
	}
	
	protected String get_group_owner(String groupID) {
		String ans = group2owner.get(groupID);
		if (ans == null)
			return "";
		else
			return ans;
	}
	
	/*
	 * 上传离线消息
	 */
	protected void update_offline_message(String to, Message msg){
		if (canUpload == false)
			return;
		
		int type = msg.get_type();
		String msg_from = msg.get_from();
		String msg_to = msg.get_to();
		String info = msg.get_msg(); 
		boolean is_group = msg.get_isgroup();
		String isgroup = "";
		if (is_group) isgroup = "T";
				else  isgroup = "F"; 
		
		String msg_id = si.getID();
		DBControl db = new DBControl(true);
		
		String sql = "insert into message(msg_id,msg_type,msg_from,msg_to,is_group,msg) VALUES(?,?,?,?,?,?)";
		PreparedStatement stmt = db.getStatement(sql);
		
		try {
			stmt.setString(1, msg_id);
			stmt.setInt(2, type);
			stmt.setString(3, msg_from);
			stmt.setString(4, msg_to);
			stmt.setString(5, isgroup);
			stmt.setString(6, info);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.clean();
		}
		
		db = new DBControl(true);
		
		sql = "insert into message_send(msg_id,send_to,have_send) VALUES(?,?,?)";
		stmt = db.getStatement(sql);
		
		try {
			stmt.setString(1, msg_id);
			stmt.setString(2, to);
			stmt.setString(3, "F");
			stmt = null;
			db.update();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.clean();
		}
		
		File filepath = new File("msg_id.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
			bw.write(msg_id);
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println(msg_id);
			e.printStackTrace();
		} finally {
		}
	}
	
	protected Vector<Message> get_offline_message(String to){
		Vector<Message> ans = new Vector<Message>();
		DBControl db = new DBControl(true);
		String sql = "select a.msg_id as msg_id, a.msg_type as msg_type, a.msg_from as msg_from, "
				+ "a.msg_to as msg_to, a.is_group as is_group, a.msg as msg "
				+ "from message as a, message_send as b where "
				+"a.msg_id=b.msg_id and b.send_to=? and b.have_send='F'";
		PreparedStatement stmt = db.getStatement(sql);
		ResultSet rs = null;
		try {
			stmt.setString(1, to);
			stmt = null;
			rs = db.query();
			if (rs!=null)
				while (rs.next()){
					String msg_id = rs.getString("msg_id");
					int msg_type = rs.getInt("msg_type");
					String msg_from = rs.getString("msg_from");
					String msg_to = rs.getString("msg_to");
					String is_group = rs.getString("is_group");
					boolean isgroup = is_group.equals("T");
					String msg = rs.getString("msg");
					Message tmp_msg = new Message(msg_type, msg_from, msg_to, isgroup, msg);
					tmp_msg.set_id(msg_id);
					ans.addElement(tmp_msg);
					
				}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.closeResultSet(rs);
			db.clean();
		}
		return ans;
	}
	
	protected boolean update_send_message(String msg_id, String to){
		DBControl db = new DBControl(true);
		String sql = "UPDATE message_send SET have_send='T' where msg_id=? and"
				+ " send_to=? and have_send='F'"; 
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setString(1, msg_id);
			stmt.setString(2, to);
			stmt = null;
			db.update();
		} catch (SQLException e) {
			db.clean();
			e.printStackTrace();
			return false;
		} finally {
			db.clean();
		}
		return true;
	}
	
	private Server() {
		load_Account();

		try {
			ServerSocket serversocket = new ServerSocket(1234);
			Socket socket = null;

			int count = 0;
			System.out.println("server to start");
		
			while (true) {
				// a new client connect in
				socket = serversocket.accept();
				ServerThread serverthread = new ServerThread(this, socket);
				serverthread.start();

				count += 1;
				System.out.println("client num : " + count);
				InetAddress address = socket.getInetAddress();
				System.out.println("current server ip: " + address.getHostAddress());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}

	public static void main(String[] args) {
		Server startServer = new Server();
	}
}
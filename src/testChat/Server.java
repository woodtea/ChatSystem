package testChat;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;
import java.nio.channels.*;

import java.sql.*;
import com.mchange.v2.c3p0.*;

/*
 * �뷨��
 * 		���Ҿ��ö��ں�����Ⱥ��Ϣ����ֱ���ÿͻ���������
 * 		����϶��ͻ���֪���Լ��˺ŵĺ�����Ϣ��Ⱥ��Ϣ�����
 * 		��ȫ����Ҫ�������������ж�.ֱ��������ת������.
 * 		server����һ��·�ɵĹ���.
 */

/*
 * �¼�:
 * ConcurrentHashMap account2id
 * ConcurrentHashMap id2account
 * ɾ��:
 * OnlineAccount
 */

/*
 * ToDoList:
 * �������޸�����
 * �ڼ��ܴ�ʩ
 * 
 */

/*
 * default encoding format:utf-8
 * 
 * Ĭ����������:
 *		1)���������� _ �ָ�������
 *		2)����,���������ô�д����.
 */
public class Server {

	ConcurrentHashMap<String, String> account;
	ConcurrentHashMap<String, ServerThread> accountServer;
	ConcurrentHashMap<String, Vector<String>> groupMember;

	// @ �¼�
	ConcurrentHashMap<String, Integer> account2id;
	ConcurrentHashMap<Integer, String> id2account;

	private final static String separator = System.getProperty("file.separator");
	private final static String line = System.lineSeparator();

	Integer accountNo = 0;

	/*
	 * �ӷ���������ָ�����ļ�����ָ�����˺��� ������(ÿ�������ʽΪ: �˺��� ���� ) ��ʼ����������
	 *
	 * Ĭ��Server��ʼ����load_Account��ʼ
	 */
	private void load_Account() {
		account = new ConcurrentHashMap<String, String>();
		accountServer = new ConcurrentHashMap<String, ServerThread>();
		groupMember = new ConcurrentHashMap<String, Vector<String>>();
		account2id = new ConcurrentHashMap<String, Integer>();
		id2account = new ConcurrentHashMap<Integer, String>();

		account.clear();
		accountServer.clear();
		groupMember.clear();
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
	}

	/*
	 * ע���û������Ӧ���룬��������޷�ͨ���� 1)�����Ѿ���ע��. 2)����̫��(����ɿͻ����ж�)
	 * 
	 * �����������ֵΪ������ϸ��Ϣ������Ϊnull
	 * 
	 */
	protected String sign_up(String name, String passwd) {
		if (account.containsKey(name) == true)
			return "The account name already exists!";

		int newAccountNo = 0;
		synchronized (accountNo) {
			accountNo += 1;
			newAccountNo = accountNo;
		}

		account.put(name, passwd);
		account2id.put(name, newAccountNo);
		id2account.put(newAccountNo, name);

		DBControl db = new DBControl(true);

		String sql = "INSERT INTO account(id,name,password) VALUE(?,?,?)";
		PreparedStatement stmt = db.getStatement(sql);

		try {
			stmt.setInt(1, newAccountNo);
			stmt.setString(2, name);
			stmt.setString(3, passwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;

		db.update();

		db.clean();
		return null;
	}

	/*
	 * ��¼�û��������룬��������޷�ͨ���� 1�����ֲ����� 2��������� 3���Ѿ���¼
	 * ���ڵ�¼�ɹ����û�������accountServer��¼���ӦServer�߳�.
	 * 
	 */
	protected String sign_in(String name, String passwd, ServerThread server) {
		if (account.containsKey(name) == false)
			return "The account name doesn't exist!";
		if (accountServer.containsKey(name) == true)
			return "The account already sign in!";
		if (account.get(name).equals(passwd) == false)
			return "wrong password!";
		accountServer.put(name, server);
		return null;
	}

	/*
	 * ÿ���˺���Ҫ��¼����Ϣ: ��ͷ��(��update),���� �����к������Ѿ������Ⱥ�� ��δ���յ���Ϣ
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
	 * ����Ⱥ��Ϣ
	 * 
	 */
	protected String get_group(int id){
		return null;
	}

	protected String get_friend(int id) {
		String result = null;
		DBControl db = new DBControl(true);

		String sql = "select b.friend_id as id,c.name as name from account as a,friend as b,"
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
					// Ҫ��������û��\n
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
	 * �޸����룬���Ӧ�����ڵ�¼֮������Ĳ���. ������������û���ر𷽱�ļ��ɣ��������������һ�(�Ժ�)
	 * 
	 * ���������������û����������룬������
	 * 
	 * �����������ֵΪ��ϸ������Ϣ������Ϊnull
	 * 
	 * ͬʱ�޸������ͬʱҪ�޸ı��ط������洢���û�-�����ļ� ��֤���ص��������ȷ
	 */
	protected String modify_passwd(String name, String oldpasswd, String newpasswd) {
		if (account.containsKey(name) == false)
			return "The account name doesn't exists!";
		if (account.get(name).equals(oldpasswd) == false)
			return "Wrong old password!";
		account.put(name, newpasswd);

		DBControl db = new DBControl(true);
		String sql = "UPDATE accout SET password=? where name=? and password=?";
		PreparedStatement stmt = db.getStatement(sql);
		try {
			stmt.setString(1, newpasswd);
			stmt.setString(2, name);
			stmt.setString(3, oldpasswd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		stmt = null;
		db.update();

		db.clean();
		return null;
	}

	/*
	 * �ر����ݿ�����
	 */
	protected void closeDB() {
		DBControl.close();
	}

	/*
	 * �����Ƿ����ĳ�����ֵ��˺�
	 */
	protected boolean check_exist(String name) {
		return account.containsKey(name);
	}

	protected boolean check_exist(int id) {
		return id2account.containsKey(id);
	}

	/*
	 * ���ĳ���˺��Ƿ����ߣ���̬��
	 */
	protected boolean check_online(String name) {
		return accountServer.contains(name);
	}

	protected boolean check_online(int id) {
		String name = id2account.get(id);
		if (name == null)
			return false;
		return accountServer.contains(name);
	}

	/*
	 * ��Ӻ���: �Ӻ���,����ĺ�����������: A�����Լ��ķ��������Ͷ�Ӧ��Ӻ��ѵ�Message��. to��ľ�����Ҫ��Ӻ��ѵ��û�B
	 * ������ֱ��ת����A��Ӧ�ķ�����,Ȼ��ת����B�Ķ�Ӧ������. Ȼ��B��Ҫ�������Լ��ķ���������ͬ���֮��Ȼ��B��Ӧ��
	 * ������֪ͨ���ķ�������Ӻ��Ѷ�.(�Գ�) ֮��ת�����Է���˫����ȡ�µĺ����б�.
	 * 
	 * Ĭ����id��ͷ,������id�����(�ɿͻ����ж�) ���id�����ڵĻ���ֱ���˳���
	 */
	protected void add_friend(int id, int friend_id) {
		/*
		 * ���ܴ������������������˫��ͬʱͬ��ӶԷ�������û�ж�id��friend_idͬ����
		 * ��˿��ܻ���ּ����ε����.(������SQL�ϵ�Ψһ�ԣ��ڶ��β�����׳�SQLException�� �����߼�Ӧ��û�����⣩
		 * 
		 * �ͻ���Ҫ����ͬ�����ע������Ѿ������д��ڸú����������Ӹð�.
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
		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.update();

		db.clean();
	}

	/*
	 * ɾ�����ѣ�ɾ������Ӧ���ǶԳƵ� A�����������ɾ������B��Message,��ô�����������
	 * ��Message���ø���B�ķ�������B����ɾ��Message. (������Ⱥ�˳���ƺ�ֵ�ÿ���)
	 * 
	 * ͬ��Ĭ����id��ͷ��������id����ȣ��ɿͻ����жϣ� ��������id�Ѿ��Ǻ�����. ���id�����ڵĻ�ֱ���˳�.
	 */
	protected void delete_friend(int id, int friend_id) {
		/*
		 * ͬ��Ӻ���,���ܴ������������������˫��ͬʱɾ�����棬�����ӵĻ� SQL��ɾ�����Σ�ͬ�����׳�Exception
		 * ɾ��ͬ���ͻ���ҲҪȷ���Ƿ��Ѿ����ں�����.
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
		} catch (SQLException e) {
			e.printStackTrace();
		}

		db.update();

		db.clean();
	}

	protected Vector<String> get_groupMember(String groupID) {
		if (groupMember.containsKey(groupID)) {
			return groupMember.get(groupID);
		}
		return null;
	}

	/*
	 * ���ض�ӦaccountID��server������.
	 * 
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
	 * ��accountServer��ɾ��ĳ�û�,Ĭ���õ������֣�
	 */
	protected void delete_accountServer(int id){
		String name=id2account.get(id);
		delete_accountServer(name);
	}
	
	protected void delete_accountServer(String name){
		System.out.println("account "+name+" log off");
		accountServer.remove(name);
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
		}
	}

	public static void main(String[] args) {
		Server startServer = new Server();
	}
}
package testChat;

import java.beans.PropertyVetoException;
import java.sql.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBControl {
	private static final String JDBC_DRIVER = Parameter.JDBC_DRIVER;
	private static final String URL = Parameter.DB_URL;
	private static final String USER = Parameter.DB_USER;
	private static final String PASSWORD = Parameter.DB_PASSWD;

	private static ComboPooledDataSource cpds;
	private Connection conn;
	private PreparedStatement stmt;
	private String sql;

	public DBControl() {
		conn = null;
		stmt = null;
	}

	public DBControl(boolean a) {
		conn = null;
		stmt = null;
		if (a)
			prepare();
	}

	public static void initialize() {
		cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass(JDBC_DRIVER);
			cpds.setJdbcUrl(URL);
			cpds.setUser(USER);
			cpds.setPassword(PASSWORD);

			cpds.setMaxPoolSize(100);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
			if (cpds != null)
				cpds.close();
		}
	}

	public void getConnection() {
		try {
			conn = cpds.getConnection();
		} catch (SQLException e) {
			System.out.println(">>SQL connection Exception<<");
			e.printStackTrace();
		}
	}

	public PreparedStatement getStatement(String sql) {
		this.sql = sql;
		try {
			stmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			System.out.println(">>SQL statement Exception<<");
			e.printStackTrace();
		}
		return stmt;
	}

	public void prepare() {
		getConnection();
	}

	public static void closeResultSet(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			System.out.println(">>SQL close resultset Exception<<");
			e.printStackTrace();
		}
	}

	public void closeStatement() {
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			System.out.println(">>SQL close statement Exception<<");
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.out.println(">>SQL close connection Exception<<");
			e.printStackTrace();
		}
	}

	public void clean() {
		closeStatement();
		closeConnection();
	}

	public ResultSet query() {
		try {
			return stmt.executeQuery();
		} catch (SQLException e) {
			System.out.println(">>SQL query Exception<<");
			System.out.println("use SQL: " + sql);
			e.printStackTrace();
		}
		return null;
	}

	public void update() {
		try {
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(">>SQL update Exception<<");
			System.out.println("use SQL: " + sql);
			e.printStackTrace();
		}
	}

	public static void close() {
		synchronized (cpds) {
			if (cpds != null)
				cpds.close();
		}
	}
	
	public void start_transaction(){
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

/*
 * 相关DB定义如下：
 * 
 * 1）账户，id,密码数据库表： create table account( id int not null unique, name
 * varchar(45) not null unique, password varchar(100) not null);
 * 
 * 2) 朋友数据库表 create table friend( id int not null, friend_id int no FOREIGN KEY
 * (id) REFERENCES account(id), FOREIGN KEY (friend_id) REFERENCES account(id));
 * 
 * 3）群 create table groups(group_id INT PRIMARY KEY,group_name varchar(300) NOT NULL,
 * 		owner_id INT NOT NULL,FOREIGN KEY(owner_id) REFERENCES account(id));   
 * 
 * 4) 群成员 create table group_member(group_id INT, member_id INT,
 * 		    FOREIGN KEY(group_id) REFERENCES groups(group_id),
 * 		    FOREIGN KEY(member_id) REFERENCES account(id));
 * 
 * 5) 消息列表 create table message( msg_id varchar(200) PRIMARY KEY, msg_type int NOT NULL, 
 * 		msg_from varchar(300), msg_to varchar(300), is_group varchar(10), msg varchar(500));
 * 
 * 6) 消息发送表 create table message_send( msg_id varchar(200),
 * 		 send_to varchar(200), have_send varchar(10), FOREIGN KEY(msg_id) REFERENCES message(msg_id));

 */

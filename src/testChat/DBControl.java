package testChat;

import java.beans.PropertyVetoException;
import java.sql.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBControl {
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost:3306/ChatSystem?autoReconnect=true&useSSL=false";
	private static final String USER = "root";
	private static final String PASSWORD = "123A(456)h";

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
 */

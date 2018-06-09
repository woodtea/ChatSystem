package testChat;

public class Parameter {
	//所有可调整的参数都被集中到这里
	//服务器的心跳包等待时间(毫秒)(可以不调)
	public static final int ServerHeartBeatTime = 15000;
	
	//客户端的心跳包等待时间(毫秒)(可以不调)
	public static final int ClientHeartBeatTime = 5000;
	
	//以下的服务器默认IP地址和端口是本地特殊的,需要根据本地情况修改
	public static final String Server_IP = "";
	
	public static final int Server_Port = 1234;
	
	//以下数据库四个参数是本地特殊的,需要根据本地数据库配置进行配置
	//JDBC参数,设置连接的数据库
	public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	
	//JDBC参数,设置连接的数据库的地址(自己为本地服务器下的名为ChaySystem的数据库)
	public static final String DB_URL = "jdbc:mysql://localhost:3306/ChatSystem?autoReconnect=true&useSSL=false";
	
	//JDBC参数,设置登录数据库的用户名
	public static final String DB_USER = "root";
	
	//JDBC参数,设置登录数据库的密码;
	public static final String DB_PASSWD = "123A(456)h";
}

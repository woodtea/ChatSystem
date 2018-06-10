package testChat;

public class Parameter {
	//所有可调整的参数都被集中到这里
	//服务器的心跳包等待时间(毫秒)(可以不调)
	public static final int ServerHeartBeatTime = 15000;
	
	//客户端的心跳包等待时间(毫秒)(可以不调)
	public static final int ClientHeartBeatTime = 5000;
	
	//是否开启终端模式,否的话则是图形界面,是的话则是终端输入消息包与输出消息包
	//测试使用终端模式,这样可以显示收到的包.
	public static final boolean isTerminal = false;
	
	//选择是本地调试模式还是连接IP地址模式(如果本地调试则Server_IP无用)
	public static final boolean isLocal = false;
	//以下的服务器默认IP地址和端口是本地特殊的,需要根据本地情况修改
	public static final String Server_IP = "10.128.171.93";
	
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
	
	//设置聊天信息记录包的大小, 10表示每个包包含了至多10条聊天记录
	public static final int ChatRecordPacketSize = 10;
	
	//是否允许服务器往终端输出心跳包,对于调试测试有些必要(因为Eclipse的终端是有输出就会自动跳到那个终端,如果
	//服务器一直在输出心跳包的话,那么我们去Client输入一些包就很麻烦(经常被切出来))
	public static final boolean canServerPrintOutHeartBeat = false;
}

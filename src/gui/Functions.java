package gui;

import javax.swing.ImageIcon;

/**
 * @author cmy
 * 如果需要返回其它的内容，请自行在该类中添加static字段，
 * 并在"需要填充的内容"下面把返回的内容做好说明
 */
public class Functions {
	
	static String inform="测试用字符串";
	static user hostUser=new user(1);
	  
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
		return true;
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
		return false;
	}
	
	
	static class user
	{
		String name;
		ImageIcon icon;
		user(int i)//这个函数是测试用的，请自行添加字段和构造函数
		{
			name="崔牧原";
			icon=new ImageIcon("cmy.jpg");
		}
		//TODO
	}
}

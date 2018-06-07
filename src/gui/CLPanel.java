package gui;
import java.awt.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public class CLPanel extends JPanel{
	Vector<Chat> chatList;
	void init()
	{
		chatList=new Vector<Chat>();
		setLayout(new GridBagLayout());
		//以下为测试用代码
		for(int i=0;i<10;i++)
		{
			chatList.add(new Chat(false,i,new ImageIcon("cmy.jpg"),"崔牧原"+i));
		}
	}
	void refresh()
	{
		removeAll();
		GridBagConstraints temp;
		int i=0;
		for(Chat c:chatList)
		{
			temp=MainUI.simpleCons(0, i++);
			add(c.but,temp);
		}
	}
	void save()
	{
		
	}
	
}

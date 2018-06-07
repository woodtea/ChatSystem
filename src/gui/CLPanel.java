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
<<<<<<< HEAD
		for(int i=0;i<10;i++)
=======
		for(int i=10;i>=0;i--)
>>>>>>> cmy_workflow
		{
			chatList.add(new Chat(false,i,new ImageIcon("cmy.jpg"),"崔牧原"+i));
		}
	}
	void refresh()
	{
		removeAll();
		GridBagConstraints temp;
		int i=0;
<<<<<<< HEAD
		for(Chat c:chatList)
		{
			temp=MainUI.simpleCons(0, i++);
			add(c.but,temp);
		}
	}
=======
		for(int j=chatList.size()-1;j>=0;j--)
		{
			temp=MainUI.simpleCons(0, i++);
			add(chatList.elementAt(j).but,temp);
		}
	}
	void toTop(Chat chat)
	{
		chatList.remove(chat);
		chatList.add(chat);
		refresh();
		this.revalidate();
		this.repaint();
		
	}
>>>>>>> cmy_workflow
	void save()
	{
		
	}
	
}

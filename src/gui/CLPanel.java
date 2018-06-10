package gui;
import java.awt.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public class CLPanel extends JPanel{
	volatile Vector<Chat> chatList;
	void init()
	{
		chatList=new Vector<Chat>();
		setLayout(new GridBagLayout());
		//以下为测试用代码
//		for(int i=10;i>=0;i--)
//		{
//			chatList.add(new Chat(false,i,new ImageIcon("cmy.jpg"),"崔牧原"+i));
//		}
//		ConcurrentHashMap<Integer, Functions.user> friendList = Functions.getFriendList();
//		for(Functions.user u : friendList.values()){
//			chatList.add(new Chat(false, u.id, u.icon, u.name));
//		}
	}
	void refresh()
	{
		removeAll();
		GridBagConstraints temp;
		int i=0;
		for(int j=chatList.size()-1;j>=0;j--)
		{
			temp=MainUI.simpleCons(0, i++);
			add(chatList.elementAt(j).but,temp);
		}
	}
	Chat toTop(Chat chat)
	{
		int index=chatList.indexOf(chat);
		if(index!=-1)
		{
			Chat oldChat=chatList.get(index);
			chatList.remove(index);
			chatList.add(oldChat);
			this.refresh();
			this.revalidate();
			this.repaint();
			return oldChat;
		}
		else
		{
			chatList.add(chat);
			this.refresh();
			this.revalidate();
			this.repaint();
			return chat;
		}
			
	}
	
	void getChatInform(Chat chat)
	{
		int index=chatList.indexOf(chat);
		if(index==-1) return;
		Chat old_chat=chatList.get(index);
		chat.mainChat=old_chat.mainChat;
		chat.mainChatNum=old_chat.mainChatNum;
		chat.send=old_chat.send;
	}
	void save()
	{
		
	}
	
}

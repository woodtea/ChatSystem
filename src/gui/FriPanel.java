package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;

public class FriPanel extends JPanel{
	volatile Set<Friend> friendList;
	volatile Set<Friend> groupList;
	void init()
	{
		friendList=new TreeSet<Friend>();
		groupList=new TreeSet<Friend>();
		setLayout(new GridBagLayout());
		//以下为测试用代码
//		for(int i=0;i<10;i++)
//		{
//			friendList.add(new Friend(false,i,new ImageIcon("cmy.jpg"),"崔牧原"+i));
//		}
		ConcurrentHashMap<Integer, Functions.user> fri_List = Functions.getFriendList();
		ConcurrentHashMap<Integer, Functions.group> gru_List = Functions.getGroupList();
		for(Functions.user u : fri_List.values()){
			friendList.add(new Friend(false, u.id, u.icon, u.name));
		}
		for(Functions.group g : gru_List.values()){
			groupList.add(new Friend(true, g.id, new ImageIcon("newFriend.png"), g.name));
		}
	}
	void refresh()
	{
		removeAll();
		GridBagConstraints temp;
		int i=0;
		for(Friend f:friendList)
		{
			temp=MainUI.simpleCons(0, i++);
			add(f.but,temp);
		}
		for(Friend g:groupList)
		{
			temp=MainUI.simpleCons(0, i++);
			add(g.but,temp);
		}
		revalidate();
		repaint();
	}
}

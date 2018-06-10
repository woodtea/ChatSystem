package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class Friend implements Comparable<Friend>{
	volatile boolean isGroup;
	volatile int id;
	volatile ImageIcon icon;
	volatile String name;
	volatile JPanel but;
	volatile JButton sendInform;
	public int compareTo(Friend f)
	{
		if(!name.equals(f.name)) return name.compareTo(f.name);
		else return Integer.compare(id, f.id);
	}
	Friend(boolean isGroup,int id,ImageIcon icon,String name)
	{
		this.isGroup=isGroup;
		this.id=id;
		this.icon=icon;
		this.name=name;
		sendInform=new JButton();
		sendInform.setText("发消息");
		sendInform.setBackground(new Color(18,150,17));
		sendInform.setFont(new Font("微软雅黑",Font.PLAIN,16));
		//sendInform.setForeground(Color.WHITE);
		sendInform.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent arg0) {
				MainUI.switchToChat();
				Chat nowChat=new Chat(isGroup,id,icon,name);
				nowChat=MainUI.midDown.toTop(nowChat);
				nowChat.toMainUI();
				
			}
		}		
		);
		
		but=new JPanel();
		//but.setUI(new BasicButtonUI());// 恢复基本视觉效果  
        but.setBorder(null);
        but.setBackground(new Color(235,233,232));
        but.setLayout(null);
        but.setPreferredSize(new Dimension(MainUI.midWidth,95));
        ImageIcon i=new ImageIcon();
        i.setImage(icon.getImage().getScaledInstance(60,60,Image.SCALE_DEFAULT));
        JLabel pic=new JLabel(i);
        pic.setBounds(18, 18, 60, 60);
        but.add(pic);
        JLabel chatName=new JLabel();
        String showName;
        if(name.length()<=9) showName=name;
        else showName=name.substring(0, 9)+"...";
        chatName.setText(showName);
        chatName.setFont(new Font("微软雅黑",Font.PLAIN,20));
        chatName.setBounds(90,34,255,28);
        but.add(chatName); 
        but.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				MainUI.friendName.setText(name);
				MainUI.fRightDown.removeAll();
				MainUI.fRightDown.add(sendInform);
			}
        }	
        );
	}
}

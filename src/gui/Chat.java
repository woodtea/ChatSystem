package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class Chat {
	boolean isGroup;
	int id;
	ImageIcon icon;
	String name;
	JPanel but;
	JPanel mainChat;
	JPanel send;
	Chat(boolean isGroup,int id,ImageIcon icon,String name)
	{
		this.isGroup=isGroup;
		this.id=id;
		this.icon=icon;
		this.name=name;
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
        chatName.setBounds(90,18,255,27);
        but.add(chatName); 
        but.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				MainUI.chatName.setText(name);
				MainUI.rightMid.setViewportView(mainChat);
				MainUI.rightPanel.remove(MainUI.rightDown);
				MainUI.rightDown=send;
				GridBagConstraints temp=MainUI.simpleCons(0, 2);
				temp.weightx=100; temp.fill=GridBagConstraints.BOTH;
				MainUI.rightPanel.add(MainUI.rightDown,temp);
			}
        }	
        );
        
        mainChat=new JPanel();
        mainChat.setLayout(new GridBagLayout());
        mainChat.setBackground(new Color(245,245,245));
        mainChat.setBorder(null);
        //mainChat.setMinimumSize(new Dimension(1000,1000));
        //mainChat.setBackground(Color.green);
        
        
        send=new JPanel();
        send.setLayout(null);
        send.setMinimumSize(new Dimension(MainUI.rightWidth,200));
        send.setPreferredSize(new Dimension(MainUI.rightWidth,200));
        JEditorPane inform=new JEditorPane();
        inform.setFont(new Font("微软雅黑",Font.PLAIN,18));
        inform.setBounds(0,0,MainUI.rightWidth,150);
        send.add(inform);
        JButton sendBut=new JButton("发送");
        sendBut.setBounds(450,160,100,35);
        sendBut.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String text=inform.getText();
				JPanel aSendMsg=new JPanel(new GridBagLayout());
				
			}
        }	
        );
        send.add(sendBut);
        
        
        
	}
}

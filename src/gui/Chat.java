package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.function.Function;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class Chat {
	static JPanel blank=new JPanel();
	boolean isGroup;
	int id;
	ImageIcon icon;
	String name;
	JPanel but;
	JPanel mainChat;
	int mainChatNum;
	JPanel send;
	Chat itself=this;
	@Override
	public boolean equals(Object o)
	{
		Chat that=(Chat)o;
		return this.id==that.id&&this.isGroup==that.isGroup;
	}
	Chat(boolean isGroup,int id,ImageIcon icon,String name)
	{
		this.isGroup=isGroup;
		this.id=id;
		this.icon=icon;
		this.name=name;
		mainChatNum=0;
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
//				mainChat.revalidate();
//				mainChat.repaint();
				GridBagConstraints t=MainUI.simpleCons(1,mainChatNum);
				t.weightx=100;
				mainChat.add(blank,t);
				
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
//        GridBagConstraints temp=MainUI.simpleCons(1, 1000000);
//        temp.ipady=100;
        //mainChat.setPreferredSize(new Dimension(MainUI.rightWidth-40,479));
        //mainChat.setBackground(new Color(245,245,245));
        //mainChat.setBackground(Color.green);
        mainChat.setBorder(null);
        //mainChat.setMinimumSize(new Dimension(1000,1000));
        //mainChat.setBackground(Color.green);
        
        
        send=new JPanel();
        send.setLayout(new GridBagLayout());
        send.setMinimumSize(new Dimension(MainUI.rightWidth,200));
        send.setPreferredSize(new Dimension(MainUI.rightWidth,200));
        send.setBackground(Color.white);
        JEditorPane inform=new JEditorPane();
        inform.setFont(new Font("微软雅黑",Font.PLAIN,18));
        //inform.setBounds(0,0,MainUI.rightWidth,150);
        inform.setMinimumSize(new Dimension(MainUI.rightWidth-25,150));
        inform.setPreferredSize(new Dimension(MainUI.rightWidth-25,150));
        GridBagConstraints temp=MainUI.simpleCons(0, 0);
        temp.fill=GridBagConstraints.HORIZONTAL;
        //temp.insets=new Insets(0,0,0,15);
        temp.weightx=100;
        send.add(inform,temp);
        
        JButton sendBut=new JButton("发送");
        //sendBut.setBounds(450,160,100,35);
        sendBut.setMinimumSize(new Dimension(100,35));
        sendBut.setPreferredSize(new Dimension(100,35));
        sendBut.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String text=inform.getText();
				JPanel aSendMsg=new JPanel(new GridBagLayout());
				//System.out.println(mainChat.getSize());
				//aSendMsg.setBackground(Color.GREEN);
				JTextArea msg=new JTextArea();
				
				ImageIcon i=new ImageIcon("fail.png");
				i.setImage(i.getImage().getScaledInstance(20,20,Image.SCALE_DEFAULT));
				JLabel failIcon=new JLabel(i);
				failIcon.setVisible(false);
				failIcon.setPreferredSize(new Dimension(20,20));
				failIcon.setMinimumSize(new Dimension(20,20));
				GridBagConstraints temp=MainUI.simpleCons(0, 0);
				temp.anchor=temp.NORTH;
				temp.ipadx=30;
				aSendMsg.add(failIcon,temp);
				
				msg.setEditable(false);
				msg.setLineWrap(true);
				msg.setWrapStyleWord(true);
				msg.setText(text);
				msg.setSize(new Dimension(350,10000));
				msg.setMinimumSize(new Dimension(350,50));
				msg.setFont(new Font("微软雅黑",Font.PLAIN,18));
				inform.setText("");
				msg.setBackground(new Color(158,234,106));
				temp=MainUI.simpleCons(1, 0);
				temp.fill=temp.VERTICAL;
				//temp.ipadx=15;
				aSendMsg.add(msg,temp);
				
				i=new ImageIcon();
				i.setImage(icon.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
				JLabel userIcon=new JLabel(i);
				userIcon.setPreferredSize(new Dimension(50,50));
				userIcon.setMinimumSize(new Dimension(50,50));
				temp=MainUI.simpleCons(2, 0);
				temp.ipadx=30;
				temp.anchor=temp.NORTHEAST;
				aSendMsg.add(userIcon,temp);
				temp=MainUI.simpleCons(0, mainChatNum);
				temp.weightx=100;
				mainChat.add(blank,temp);
				temp=MainUI.simpleCons(1, mainChatNum++);
				//temp.weightx=100;
			
				
				//aSendMsg.setPreferredSize(aSendMsg.getSize());
				//aSendMsg.setMinimumSize(new Dimension(500,100));
				//aSendMsg.setPreferredSize(new Dimension(500,100));
				temp.anchor=temp.NORTHEAST;
				//temp.fill=temp.BOTH;
				//temp.insets=new Insets(0,0,0,0);
				temp.ipady=20;
				mainChat.add(aSendMsg, temp);
				//System.out.println(aSendMsg.getSize());
				MainUI.midDown.toTop(itself);
				
				String failText=Functions.sendMsg(Functions.hostUser.id,id, isGroup, text);
				if(!failText.equals("发送成功！"))
				{
					failIcon.setVisible(true);
					failIcon.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent arg0) {
							JLabel errorLabel=new JLabel(failText);
							errorLabel.setFont(new Font("微软雅黑",Font.PLAIN,15));
							JOptionPane.showMessageDialog(null, errorLabel, "【出错啦】", JOptionPane.ERROR_MESSAGE);
						}
					});
				}
				
			}
        }	
        );
        temp=MainUI.simpleCons(0, 1);
        temp.anchor=temp.EAST;
        temp.insets=new Insets(0,0,0,15);
        send.add(sendBut,temp);
        
        
        
	}
}

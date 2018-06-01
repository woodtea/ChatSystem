package gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;
import java.awt.event.MouseAdapter;

public class MainUI extends JFrame{
	final static int height=759;
	final static int width=1101;
	final static int leftWidth=91;
	final static int midWidth=354;
	final static int rightWidth=width-leftWidth-midWidth;
	public static void main(String arg[])
	{
		run();
	}
	public static void run()
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI frame = new MainUI();
					frame.setVisible(true);
//					while(true)
//					{
//						Thread.sleep(1000);
//						System.out.println(frame.getWidth()+"*"+frame.getHeight());
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public MainUI() { 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(width,height));
		JPanel bigPanel=new JPanel();
		bigPanel.setLayout(new GridBagLayout());
		this.setContentPane(bigPanel);
		GridBagConstraints temp;
		Dimension d;
		temp=new GridBagConstraints(0,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),leftWidth-50,height);
		JPanel leftPanel=new JPanel(new GridBagLayout());
		bigPanel.add(leftPanel, temp);
		temp=new GridBagConstraints(1,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),midWidth,height);
		JPanel midPanel=new JPanel(new GridBagLayout());
		bigPanel.add(midPanel,temp);
		temp=new GridBagConstraints(2,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),rightWidth,height);
		JPanel rightPanel=new JPanel(new GridBagLayout());
		bigPanel.add(rightPanel, temp);
		
		//左侧菜单选择面板
		leftPanel.setBackground(new Color(41,43,46));
		
		JLabel userIcon=new JLabel();
		ImageIcon image=Functions.hostUser.icon;
		image.setImage(image.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		userIcon.setIcon(image);
		temp=simpleCons(0,0);
		temp.ipady=40;
		leftPanel.add(userIcon,temp);
		
		JButton check=addButton("1.2.jpg",0,1,leftPanel);
		JButton addBook=addButton("2.2.jpg",0,2,leftPanel);
		JButton collect=addButton("3.2.jpg",0,3,leftPanel);
		JButton menu=addButton("menu.png",0,10,leftPanel);
		JPanel blank=new JPanel();
		blank.setBackground(new Color(41,43,46));
//		blank.setBackground(Color.GREEN);
		temp=simpleCons(0,4,1,5);
		temp.weighty=100;
		leftPanel.add(blank, temp);
		
		//中间聊天选择面板
		JPanel midTop=new JPanel();
		midTop.setPreferredSize(new Dimension(midWidth,80));
		midTop.setBackground(new Color(238,234,232));
		temp=simpleCons(0,0);
		midPanel.add(midTop,temp);
		
		JScrollPane midDownO=new JScrollPane();
		JPanel midDown=new JPanel(new GridBagLayout());
		
		midDownO.setViewportView(midDown);
		temp=simpleCons(0,1);
		temp.weighty=100;
		midPanel.add(midDownO, temp);
		
		d=leftPanel.getMinimumSize();
		temp=new GridBagConstraints(0,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),leftWidth-(int)d.getWidth(),height-(int)d.getHeight());
		bigPanel.add(leftPanel, temp);
		d=midPanel.getMinimumSize();
		temp=new GridBagConstraints(1,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),midWidth-(int)d.getWidth(),height-(int)d.getHeight());
		bigPanel.add(midPanel,temp);
		d=rightPanel.getMinimumSize();
		temp=new GridBagConstraints(2,0,1,1,100,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),rightWidth-(int)d.getWidth(),height-(int)d.getHeight());
		bigPanel.add(rightPanel, temp);
//		leftPanel.setBackground(Color.red);
//		midPanel.setBackground(Color.green);
//		rightPanel.setBackground(Color.yellow);
		
	}
	JButton addButton(String file,int gridx,int gridy,JPanel panel)
	{
		ImageIcon image=new ImageIcon(file);
		//image.setImage(image.getImage().getScaledInstance(42,39,Image.SCALE_DEFAULT));
		JButton chat=new JButton(image);
		chat.setIcon(image);
		chat.setUI(new BasicButtonUI());// 恢复基本视觉效果  
        chat.setPreferredSize(new Dimension(image.getIconWidth(),image.getIconHeight()));// 设置按钮大小  
        chat.setContentAreaFilled(false);// 设置按钮透明  
        chat.setBorder(null);
		GridBagConstraints temp=simpleCons(gridx,gridy);
		temp.ipady=20;
		panel.add(chat,temp);
		return chat;
	}
	GridBagConstraints simpleCons(int gridx,int gridy)
	{
		GridBagConstraints temp=new GridBagConstraints();
		temp.gridx=gridx; temp.gridy=gridy;
		return temp;
	}
	GridBagConstraints simpleCons(int gridx,int gridy,int gridWidth,int gridHeight)
	{
		GridBagConstraints temp=new GridBagConstraints();
		temp.gridx=gridx; temp.gridy=gridy;
		temp.gridwidth=gridWidth; temp.gridheight=gridHeight;
		return temp;
	}
}

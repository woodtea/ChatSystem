package gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

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
		JPanel leftPanel=new JPanel();
		bigPanel.add(leftPanel, temp);
		temp=new GridBagConstraints(1,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),midWidth,height);
		JPanel midPanel=new JPanel();
		bigPanel.add(midPanel,temp);
		temp=new GridBagConstraints(2,0,1,1,0,100,
				 GridBagConstraints.CENTER,GridBagConstraints.BOTH,
				 new Insets(0,0,0,0),rightWidth,height);
		JPanel rightPanel=new JPanel();
		bigPanel.add(rightPanel, temp);
		leftPanel.setBackground(new Color(41,43,46));
		leftPanel.setLayout(new GridBagLayout());
		JLabel userIcon=new JLabel();
		ImageIcon image=Functions.hostUser.icon;
		image.setImage(image.getImage().getScaledInstance(50,50,Image.SCALE_DEFAULT));
		userIcon.setIcon(image);
		temp=simpleCons(0,0);
		leftPanel.add(userIcon,temp);
		
		
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

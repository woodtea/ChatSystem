package gui;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Function;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import testChat.Client;
public class Log {
	static JFrame frame;
	public static void main(String[] args) {    
		Client client = new Client();
		Functions.setClient(client);
		client.start();
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame("Login");
 
        frame.setSize(558, 447);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationByPlatform(true);
      

        
        JPanel panel = new JPanel();   
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        frame.setContentPane(panel);
        
        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel contentPane) {
    	contentPane.setLayout(null);
		
		JLabel picture = new JLabel();
		picture.setBounds(0, 0, 558, 132);
		ImageIcon image=new ImageIcon("lake.jpg");
		image.setImage(image.getImage().getScaledInstance(558,132,Image.SCALE_DEFAULT));
		picture.setIcon(image);
		contentPane.add(picture);
		
		JLabel usrLabel = new JLabel("用户名");
		usrLabel.setBounds(30, 162, 81, 27);
		contentPane.add(usrLabel);
		
		JTextField usrText = new JTextField();
		usrText.setBounds(185, 162, 304, 27);
		contentPane.add(usrText);
		usrText.setColumns(10);
		
		JLabel passwordLabel = new JLabel("密码");
		passwordLabel.setBounds(30, 225, 81, 27);
		contentPane.add(passwordLabel);
		
		JPasswordField password = new JPasswordField();
		password.setBounds(185, 225, 304, 27);
		contentPane.add(password);
		password.setColumns(10);
		
		JLabel inform=new JLabel();
		inform.setBounds(30, 285, 400, 27);
		inform.setForeground(Color.RED);
		contentPane.add(inform);
		
		JButton login = new JButton("登录");
		login.setBounds(80, 340, 123, 29);
		
		contentPane.add(login);
		
		login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean success=Functions.log(usrText.getText(), password.getPassword());
				if(success)
				{
					frame.dispose();
					MainUI.run();
				}
				else
				{
					inform.setText("用户名或密码不正确");
				}
			}
		});
		
		JButton register = new JButton("注册");
		register.setBounds(339, 340, 123, 29);
		contentPane.add(register);
		
		register.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean success=Functions.register(usrText.getText(), password.getPassword());
				if(success)
				{
					inform.setText("注册成功！");
				}
				else
				{
					inform.setText(Functions.inform);
				}
			}
		});
		
    }
}

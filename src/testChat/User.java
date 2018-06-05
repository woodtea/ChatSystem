package testChat;

import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class User {
	String name;
	int id;
	ImageIcon image;
	public User(String name, int id) {
		this.name = name;
		this.id = id;
		this.image = null;
	}
	public User(String name, int id, String path) {
		try {
			this.name = name;
			this.id = id;
			this.image = new ImageIcon(ImageIO.read(new File(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public User(String name, int id, ImageIcon image) {
		this.name = name;
		this.id = id;
		this.image = image;
	}
}

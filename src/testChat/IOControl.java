package testChat;

import java.io.*;
import java.net.*;
import java.util.*;

public class IOControl {
	private final static String line=System.lineSeparator();
	public static void print(PrintWriter pw, String msg){
		pw.write(msg+line);
		pw.flush();
	}
	
	public static void print(ObjectOutputStream os, Message msg){
		try{
			os.writeObject(msg);
			os.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static Message read(ObjectInputStream os){
		Message msg=null;
		try{
			msg=(Message)os.readObject();
		}catch(EOFException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return msg;
	}
}

package testChat;

import java.util.concurrent.*;

public class Group {
	String name;
	int id;
	int owner;
	CopyOnWriteArrayList<Integer> member;
	public Group(String name, int id, int owner, CopyOnWriteArrayList<Integer> member) {
		this.name = name;
		this.id = id;
		this.owner = owner;
		this.member = member;
	}
}

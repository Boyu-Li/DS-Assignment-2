package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sql.rowset.spi.TransactionalWriter;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.omg.CORBA.TRANSACTION_MODE;

public class Task implements Callable<Boolean> {
	public Boolean ready;
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private String username = null;
	private String[] inMessage = null;
	private ServerWindow sw; 
	private int table = 0;
	private WaitingThread wt;
	public Future<Boolean> f;
	public GameThread gt;
	
	public Task(Socket client, ServerWindow sw, WaitingThread wt) {
		this.client = client;
		ready = false;
		this.sw = sw;
		try {
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
		}catch(Exception e) {
		}
		this.wt = wt;
		username = Trans.read(in)[1];
	}
	@Override
	public Boolean call() {
		String[] message;
		//sw.appendMessage(username + " log in!\n");
		/*
		if(table == 0)
			while(true) {
				message = input();
				if(message[0].equals("join")) {
					if(message[1].equals("table1"))
						this.table = 1;		
					else
						this.table = 2;
					return true;
				}
				else if(message[1].equals("exit")) {
					table = -1;
					return false;
				}
			}
		message = input();
		if(message[1].equals("ready")) {
			sw.appendMessage(username + " is ready!\n");
			ready = true;
		}
		else if(message[1].equals("exit"))
			return false;
		else if(message[0].equals("leave")) {
			leave = true;
			gt.leave();
			wt.join(client);
			return false;
		}
		while(true) {
			message = input();
			if(message[1].equals("exit")) 
				break;
			if(message[1].equals("Y")) {	
				ready = false;
				call();
				return true;
			}
			if(message[1].equals("N")) {
				leave = true;
				gt.leave();
				wt.join(client);
				return false;
			}
			inMessage = new String[message.length];
			for(int i = 0; i< message.length; i++)
				inMessage[i]= new String(message[i]);
		}
		*/
		while(true) {
			message = input();
			if(message[1].equals("ready")) {
				sw.appendMessage(username + " is ready!\n");
				ready = true;
			}
			else if(message[1].equals("exit")) {
				gt.leave(this, this.f);
				gt.disconnect(this);
				wt.refresh();
				wt.deleteUser(username);
				break;
			}
			else if(message[0].equals("leave") || message[1].equals("N")) {
				ready = false;
				gt.leave(this, f);
				gt = null;
				inMessage = null;
				table = 0;
				wt.userJoin(this, this.f);
				wt.refresh();	
				continue;
			}
			else if(message[1].equals("Y")) {	
				ready = false;
				inMessage = null;
				continue;
			}
			else if(message[0].equals("join")) {
				table = Integer.parseInt(message[1].substring(5));
			}
			else if(message[0].equals("chat")) {
				gt.groupSend(100, message);
			}
			inMessage = new String[message.length];
			for(int i = 0; i< message.length; i++)
				inMessage[i]= new String(message[i]);
		}
		return true;
	}
	
	public void turn(int round) {
		String[] message = new String[2];
		message = new String[2];
		message[0] = "turn";
		message[1] = Integer.toString(round);
		output(message);
	}
	
	public void disconnect(String user) {
		String[] message = new String[3];
		message[0] = "alert";
		message[1] = "disconnected";
		message[3] = user;
		output(message);
		ready = false;
	}
	
	private String[] input() {
		return Trans.read(in);
	}
	public synchronized void output(String[] message) {
		Trans.send(out,message);
	}
	
	public Boolean isReady() {
		return ready;
	}
	public void setTable(int table) {
		this.table = table;
	}
	public void setGT(GameThread gt) {
		this.gt = gt;
	}
	public String getUsername() {
		return this.username;
	}
	public String[] getInMessage() {
		if(inMessage == null)
			return null;
		String[] message = new String[inMessage.length];
		for(int i = 0; i< message.length; i++)
			message[i]= new String(inMessage[i]);
		inMessage = null;
		return message;
	}
	public Socket getSocket() {
		return this.client;
	}
	
	public int getTable() {
		return this.table;
	}
}


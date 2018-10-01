package Server;

import java.util.ArrayList;
import java.util.concurrent.Future;

import javax.annotation.processing.RoundEnvironment;
import javax.xml.ws.handler.MessageContext;

public class GameThread extends Thread {
	private ArrayList<Future<Boolean>> fList;
	private ArrayList<Task> tList;
	public Boolean on = false;
	private Boolean[] pass;
	
	public GameThread(ArrayList<Future<Boolean>> fList, ArrayList<Task> tList) {
		this.fList = fList;
		this.tList = tList;
	}
	
	public void run() {
		Boolean ready = false;
		while(true) {
			try {
				Thread.sleep(3000);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			for(int i=0; (i<tList.size()) && (tList.size() > 1);i++) {
				if(!tList.get(i).isReady()) {
					ready = false;
					break;
				}
				ready = true;
			}
			if(ready == true)
				break;
			ready = false;
		}
		on = true;
		ready = true;
		
		game();	
		on = false;
	}
	
	public void game(){
		pass = new Boolean[tList.size()];
		String[] message = new String[4 + tList.size()];
		message[0] = "alert";
		message[1] = "start";
		message[2] = Integer.toString(tList.size());
		for(int i = 0; i < tList.size(); i++)
			message[4 + i] = tList.get(i).getUsername();
		for(int i = 0; i < tList.size(); i++) {
			message[3] = Integer.toString(i);
			tList.get(i).output(message);
		}
		int[] score = new int[tList.size()];
		
		int count = 0;
		int round = 0;
		Boolean end = false;
		while(end == false) {
			round++;
			for(int i=0; i< tList.size(); i++)
				pass[i] = false; 
			for(int turn=0; turn<tList.size(); turn++) {
				if(count++==400)
					break;
				tList.get(turn).turn(round);
				message = getMessage(turn);
				groupSend(turn, message);
				message = getMessage(turn);
				if(message[1].equals("pass")) {
					pass[turn] = true;
					continue;
				}
				groupSend(turn, message);			
				message = new String[3];
				message[0] = "score";
				message[1] = Integer.toString(turn);
				if(vote(turn)) {
					//score[turn]++;
					message[2] = "plus";	
				}
				else {
					message[2] = "unchanged";
				}
				groupSend(100, message);
			}
			for(int i = 0; i< tList.size(); i++) {
				end = true;
				if(pass[i] == false) {
					end = false;
					break;
				}
			}
			if(count == 400)
				end = true;
		}
		message = new String[2];
		message[0] = "alert";
		message[1] = "gameover";
		groupSend(100,message);
	}
	
	public Boolean vote(int i){
		String[] message;
		Boolean agreed = true;
		for(int j=0; j<tList.size(); j++) {
			if(j == i)
				continue;
			message = getMessage(j);
			if(message[1].equals("disagree"))
				agreed = false;
		}
		return agreed;
	}
	
	public void groupSend(int n, String[] message){
		for(int j = 0; j<tList.size(); j++) {
			tList.get(j).output(message);
		}
	}
	
	public void disconnect() {
		this.interrupt();
	}
	
	public String[] getMessage(int i) {
		String[] message = null;
		while(message == null) {
			message = tList.get(i).getInMessage();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return message;
	}
}

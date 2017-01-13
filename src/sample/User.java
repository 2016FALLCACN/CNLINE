package sample;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.lang.Comparable;

/**
 * Created by leinadshih on 12/28/16.
 */
public class User {
    public ArrayList<Friend> friends = new ArrayList<Friend>();

    public String username = "";

    public User(ArrayList<String> strings){
        for(int i = 0; i < strings.size(); i++){
            this.friends.add(new Friend(strings.get(i)));
        }
        friends.sort(new Comparator<Friend>() {
        	@Override
        	public int compare(Friend friend1, Friend friend2) {
        		return friend1.getName().compareTo(friend2.getName());
        	}
        });
    }

    public Friend getFriend(String ask){
        for (Friend friend: friends) {
            if(friend.getName().equals(ask)){
                return friend;
            }
        }
        return null;
    }

    public void clear(){
        username = "";
        friends.clear();
    }
}

package sample;

import java.util.ArrayList;

/**
 * Created by leinadshih on 12/28/16.
 */
public class User {
    public ArrayList<Friend> friends = new ArrayList<Friend>();

    public User(ArrayList<String> strings){
        for(int i = 0; i < strings.size(); i++){
            this.friends.add(new Friend(strings.get(i)));
        }
    }
}

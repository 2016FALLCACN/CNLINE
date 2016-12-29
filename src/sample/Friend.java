package sample;

import java.util.ArrayList;

/**
 * Created by leinadshih on 12/28/16.
 */
public class Friend {
    private String name = "";
    private ArrayList<String> messages = new ArrayList<String>();

    public Friend(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addMessage(String msg){
        messages.add(msg);
    }
}

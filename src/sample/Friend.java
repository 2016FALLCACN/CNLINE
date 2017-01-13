package sample;

import java.util.ArrayList;

/**
 * Created by leinadshih on 12/28/16.
 */
public class Friend {
    private String name = "";
    private ArrayList<String> messages = new ArrayList<String>();
    private ArrayList<String> files = new ArrayList<String>();

    public Friend(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public ArrayList<String> getFiles() { return files; }

    public void addMessage(String msg){
        messages.add(msg);
    }

    public void addFile(String file){ files.add(file); }

    public void setAllFile(ArrayList<String> manyfile){
        this.files.clear();
        for(int i = 0; i < manyfile.size(); i++){
            this.files.add(manyfile.get(i));
        }
    }
}

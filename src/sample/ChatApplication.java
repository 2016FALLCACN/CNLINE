package sample;

import io.socket.client.*;
//import com.github.nkzawa.socketio.client.*;

import java.net.URISyntaxException;

public class ChatApplication{

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constants.SERVERURL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}

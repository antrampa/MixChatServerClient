import javax.swing.*;

public class MixChatServer {

    public static void main(String[] args) {
        System.out.println("Hello MixChatServer Here!");
        Server app = new Server();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.runServer();
    }
}

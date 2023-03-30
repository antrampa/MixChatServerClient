import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {

    private JTextField enterField; //Chat text
    private JTextArea displayArea;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = ""; //Message from the Server
    private String chatServer; //Server host address
    private Socket client;

    public Client(String host) {
        super("MixChat Client");

        chatServer = host;

        enterField = new JTextField();
        enterField.setEditable(false);
        enterField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        sendData(e.getActionCommand());
                        enterField.setText("");
                    }
                }
        );
        add(enterField, BorderLayout.NORTH);

        displayArea = new JTextArea();
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setSize(500,300);
        setVisible(true);
    }

    public void runClient() {
        try{
            connectToServer();
            getStreams();
            processConnection();
        } catch(EOFException eofException) {
            displayMessage("\nClient terminated connection");
        } catch(IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        displayMessage("Attempting connection\n");

        //Create a socket to connect with server in the port 12345
        client = new Socket(InetAddress.getByName(chatServer), 12345);

        displayMessage("Connected to: " + client.getInetAddress().getHostName());
    }

    private void getStreams() throws IOException {
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush();

        input = new ObjectInputStream(client.getInputStream());

        displayMessage("\nGot I/O streams\n");
        //displayMessage("\nConnected to: " + client.getInetAddress().getHostName());
    }

    private void processConnection() throws IOException {
        setTextFieldEditable(true);

        do {
            try {
                message = (String) input.readObject(); //Read message from Server
                displayMessage("\n" + message);
            } catch(ClassNotFoundException classNotFoundException) {
                displayMessage("\nUnknown object type received");
            }
        }while (!message.equals("SERVER>>> TERMINATE"));
    }

    private void closeConnection() {
        displayMessage("\nClosing connection");
        setTextFieldEditable(false);

        try {
            output.close();
            input.close();
            client.close(); //Close socket
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }

    }

    private void sendData(String message) {
        try {
            String msg = "\nCLIENT>>> " + message;
            output.writeObject(msg);
            output.flush();
            displayMessage(msg);
        } catch (IOException ioException) {
            displayArea.append("\nError writing object");
        }
    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        displayArea.append(messageToDisplay);
                    }
                }
        );
    }

    private void setTextFieldEditable(final  boolean editable) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        enterField.setEditable(editable);
                    }
                }
        );
    }
}

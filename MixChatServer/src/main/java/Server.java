import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server  extends JFrame {

    private JTextField enterField; //Enter messages from the user
    private JTextArea displayArea; //Show messages to the user
    private ObjectOutputStream output; //output stream to the client
    private ObjectInputStream input; //input stream from the client
    private ServerSocket server;
    private Socket connection;
    private int counter = 1;

    public Server() {
        super("MixChat Server");

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

        setSize(500, 300);
        setVisible(true);
    }

    public void runServer() {
        try {
            server = new ServerSocket(12345, 100);
            while (true) {
                try {
                    waitForConnection();
                    getStreams();
                    processConnection();
                } catch (EOFException eofException) {
                    displayMessage("\nServer terminated connection");
                } finally {
                    closeConnection();
                    ++counter;
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        displayMessage("Waiting for connection\n");
        connection = server.accept(); //Accept connection from the client
        displayMessage("Connection " + counter + " received from: "
                + connection.getInetAddress().getHostName());
    }

    private void getStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush(); //clean output buffer

        input = new ObjectInputStream(connection.getInputStream());

        displayMessage("\nGot I/O streams\n");
    }

    private void processConnection() throws IOException {
        String message = "Connection successful";
        sendData(message);// send to the client

        setTextFieldEditable(true); //Allow message chatting

        do{
            try{
                message = (String) input.readObject(); //Read message from client
                displayMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException) {
                displayMessage("\nUnknow object type received");
            }
        } while (!message.equals("CLIENT>>> TERMINATE\n"));
    }

    private void closeConnection() {
        displayMessage("\nTerminating connection\n");
        setTextFieldEditable(false);

        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void sendData(String message) {
        try {
            output.writeObject("SERVER>>> " + message);
            output.flush();
            displayMessage("\nSERVER>>> " + message);
        } catch (IOException ioException) {
            displayArea.append("\nError writing object");
        }
    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        displayArea.append(messageToDisplay); //Show the message
                    }
                }
        );
    }

    private void setTextFieldEditable(final boolean editable) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        enterField.setEditable(editable);
                    }
                }
        );
    }
}

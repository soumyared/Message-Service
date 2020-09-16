/*
NAME: REDDAMMAGARI SREE SOUMYA
ID: 1001646494
NET-ID: sxr6494 */
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;


public class MessagingClient extends JFrame implements ActionListener {

    private JTextField serverAddressTextField = new JTextField(20);
    private JTextField usernameTextField = new JTextField(10);
    private JTextField messageTextField = new JTextField(20);
    private JButton connectDisconnectButton = new JButton("Connect");
    private JButton sendMessageButton = new JButton("Send");
    private JPanel usersPanel = new JPanel();
    private JList messagesList = new JList(new DefaultListModel());

    private Map<String, JCheckBox> usersCheckBox = new HashMap<>();

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    // Setup the layout of the client
    public MessagingClient() {
        super("Messaging Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // UI for connecting to server
        /*used JPanel https://www.geeksforgeeks.org/java-swing-jpanel-examples/ link for seting up panel properties*/
        JPanel connectionPanel = new JPanel(new FlowLayout());
        add(BorderLayout.NORTH, connectionPanel);

        connectionPanel.add(new JLabel("Server Address:"));
        connectionPanel.add(serverAddressTextField);
        connectionPanel.add(new JLabel("Username:"));
        connectionPanel.add(usernameTextField);
        connectionPanel.add(connectDisconnectButton);

        connectDisconnectButton.addActionListener(this);

        // UI for displaying online users

        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBackground(Color.WHITE);
        usersPanel.setPreferredSize(new Dimension(150, 0));
        usersPanel.setBorder(BorderFactory.createEtchedBorder());
        add(BorderLayout.WEST, usersPanel);

        // UI for displaying messages
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(BorderLayout.CENTER, new JScrollPane(messagesList));

        JPanel sendMessagePanel = new JPanel(new FlowLayout());
        sendMessagePanel.add(new JLabel("Message (Checked users will receive the message): "));
        sendMessagePanel.add(messageTextField);
        sendMessagePanel.add(sendMessageButton);
        sendMessageButton.addActionListener(this);
        messagesPanel.add(BorderLayout.SOUTH, sendMessagePanel);

        add(BorderLayout.CENTER, messagesPanel);
        sendMessageButton.setEnabled(false);
    }

    // Initiate a connection to server
    private void connectToServer() {
        String serverAddress = serverAddressTextField.getText().trim();
        String username = usernameTextField.getText().trim();

        if (serverAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A server address is required.");
            return;
        }

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A username is required.");
            return;
        }

        try {
            Socket socket = new Socket(serverAddress, 8080);

            // Send a welcome message to the server
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());

            oos.writeObject(new Message("CONNECT", username));

            Message message = (Message) ois.readObject();
            JOptionPane.showMessageDialog(this, message.getData());

            if (message.getMessage().equals("INVALID")) {
                return;
            }

            // Start a new thread if username is valid, request for active users
            oos.writeObject(new Message("ACTIVE USERS"));
            new Thread(new MessageThread()).start();

            sendMessageButton.setEnabled(true);
            usernameTextField.setEditable(false);
            serverAddressTextField.setEditable(false);
            connectDisconnectButton.setText("Disconnect");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection invalid.");
        }
    }

    // Disconnect from server
    private void disconnectFromServer() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect?") != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            oos.writeObject(new Message("DISCONNECT"));
        } catch (Exception e) {
        }

        System.exit(0);
    }

    // Send message to selected users
    private void sendMessage() {
        String message = messageTextField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        message = usernameTextField.getText() + ": " + message;

        // Get only receipients selected
        Set<String> usernames = new HashSet<>();

        for (String username : usersCheckBox.keySet()) {
            if (usersCheckBox.get(username).isSelected()) {
                usernames.add(username);
            }
        }

        try {
            // Send it
            oos.writeObject(new Message("DELIVER MESSAGE", usernames, message));
            messageTextField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection to server was lost.");
            System.exit(0);
        }
    }

    // Handle button events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Connect")) {
            connectToServer();
        } else if (e.getActionCommand().equals("Disconnect")) {
            disconnectFromServer();
        } else if (e.getActionCommand().equals("Send")) {
            sendMessage();
        }
    }

    // Start the program
    public static void main(String[] args) {
        new MessagingClient().setVisible(true);
    }

    // Waits for server messages
    private class MessageThread implements Runnable {

        // Retrieve the server messages
        @Override
        public void run() {
            try {
                while (true) {
                  //ArrayList<String> ulist = new ArrayList<String>();
                  //int count=0;
                  /*setting the state of checkbox
                  http://www.java2s.com/Code/JavaAPI/javax.swing/JCheckBoxisSelected.htm*/
                    Message message = (Message) ois.readObject();

                    if (message.getMessage().equals("ACTIVE USERS")) {
                        // We got active users we have to add
                        for (String username : (Set<String>) message.getData()) {
                            JCheckBox checkBox;

                            if (username.equals(usernameTextField.getText())) {
                                checkBox = createReadOnlyCheckBox(username);
                            } else {
                                checkBox = new JCheckBox(username);
                            }

                            checkBox.setSelected(true);
                            usersPanel.add(checkBox);
                            usersCheckBox.put(username, checkBox);
                        }

                        usersPanel.updateUI();
                    } else if (message.getMessage().equals("DISCONNECTED USER")) {
                        // There's a disconnected user, we remove it
                        String username = (String) message.getData();

                        if (usersCheckBox.containsKey(username)) {
                            usersPanel.remove(usersCheckBox.get(username));
                            usersPanel.updateUI();
                          //this//
                            usersCheckBox.remove(username);
                        }
                    } else if (message.getMessage().equals("CONNECTED USER")) {
                        // There's a new user, we add it
                        String username = (String) message.getData();
                      //   ArrayList<String> ulist = new ArrayList<String>();
                      //   int count=0;

                        if (!usersCheckBox.containsKey(username)) {
                            JCheckBox checkBox = new JCheckBox(username);
                            checkBox.setSelected(true);

                            usersPanel.add(checkBox);
                            usersCheckBox.put(username, checkBox);
                            usersPanel.updateUI();

                            usersCheckBox.put(username, checkBox);
                        }
                    } else if (message.getMessage().equals("MESSAGE DELIVERY")) {
                        // New message arrived
                        ((DefaultListModel) messagesList.getModel()).addElement(message.getData().toString());
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Connection to server was lost.");
                System.exit(0);
            }
        }
    }

    // Create a read-only checkbox
    private JCheckBox createReadOnlyCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);

        // Remove mouse events
        for (MouseListener mouseListener : (MouseListener[]) checkBox.getListeners(MouseListener.class)) {
            checkBox.removeMouseListener(mouseListener);
        }

        /* Remove key events
           https://stackoverflow.com/questions/4472530/disabling-space-bar-triggering-click-for-jbutton
        */
        InputMap inputMap = checkBox.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "none");
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "none");

        return checkBox;
    }
}

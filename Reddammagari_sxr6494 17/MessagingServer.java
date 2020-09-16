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


public class MessagingServer extends JFrame implements ActionListener, Runnable {
    // A Hashmap to keep list of users
    private Map<String, User> users = new HashMap<>();
    private JPanel usersPanel = new JPanel();

    // Set the UI of the server
    public MessagingServer() {
        setTitle("Messaging Server");
        setSize(300, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set the UI for disconnection
        /*used JPanel https://www.geeksforgeeks.org/java-swing-jpanel-examples/ link for setting up panel properties*/
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Checked users are online."));

        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(this);
        topPanel.add(disconnectButton);

        add(BorderLayout.NORTH, topPanel);

        // Set UI for users
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBackground(Color.WHITE);
        usersPanel.setBorder(BorderFactory.createEtchedBorder());

        add(BorderLayout.CENTER, usersPanel);

        // Server now starts
        new Thread(this).start();
    }

    // Wait for client connections
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8080);

            while (true) {
                // Wait for a new client to connect
                Socket s = ss.accept();

                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

                Message message = (Message) ois.readObject();

                // At this point we should expect the client to welcome with a CONNECT message, otherwise
                // just reject the client
                if (!message.getMessage().equals("CONNECT")) {
                    oos.writeObject(new Message("INVALID", "Unknown welcome message."));
                    s.close();
                    continue;
                }

                /* Check the validity of the username
                setting the state of checkbox
                http://www.java2s.com/Code/JavaAPI/javax.swing/JCheckBoxisSelected.htm*/
                String username = (String) message.getData();
                User user;

                if (users.containsKey(username)) {
                    user = users.get(username);

                    if (user.checkBox.isSelected()) {
                        oos.writeObject(new Message("INVALID", "Username is online."));
                        continue;
                    }

                    user.inputStream = ois;
                    user.outputStream = oos;
                } else {
                    user = new User(createReadOnlyCheckBox(username), ois, oos);
                    usersPanel.add(user.checkBox);
                }

                /* Broadcast to online users about the new user*/
                broadcastMessage(users.keySet(), new Message("CONNECTED USER", username));

                // Mark the user online
                user.checkBox.setSelected(true);
                usersPanel.updateUI();

                users.put(username, user);
                String x="";
                for ( String key : users.keySet() ) {
                     System.out.println( key );
                     x=x+","+key;
                     }

                // Create another thread that handles specificlly this user
                oos.writeObject(new Message("VALID", "Welcome " + username + "!\n"+" List of usernames\n"+x));
                //oos.writeObject(new Message("VALID", "Welcome " + "ola" + "!"));
                new Thread(new UserThread(username)).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    // Send a message to the selected users
    private void broadcastMessage(Set<String> usernames, Message message) {
        for (String username : usernames) {
            if (!users.containsKey(username)) {
                // Don't send to a non-existing user
                continue;
            }

            User user = users.get(username);

            if (!user.checkBox.isSelected()) {
                // Don't send to an offline user
                continue;
            }

            try {
                user.outputStream.writeObject(message);
            } catch (Exception e) {
            }
        }
    }

    // Handle button actions, there's only one button at the moment, that is to disconnect the server
    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to stop the server?") == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Start the server
    public static void main(String[] args) {
        new MessagingServer().setVisible(true);
    }

    // Tracks user info
    private class User {

        public JCheckBox checkBox;
        public ObjectInputStream inputStream;
        public ObjectOutputStream outputStream;

        // Create a new user
        public User(JCheckBox checkBox, ObjectInputStream inputStream, ObjectOutputStream outputStream) {
            this.checkBox = checkBox;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }
    }

    // A thread to accept and respond to a user's request
    private class UserThread implements Runnable {

        public String username;

        // Create a thread
        public UserThread(String username) {
            this.username = username;
        }

        // Run and wait for requests from user
        @Override
        public void run() {
            User user = users.get(username);

            try {
                ObjectInputStream ois = user.inputStream;
                ObjectOutputStream oos = user.outputStream;

                while (true) {
                    // Wait for a message to broadcast to other users
                    Message message = (Message) ois.readObject();

                    // Interpret the request of the user
                    if (message.getMessage().equals("DELIVER MESSAGE")) {
                        // Deliver the message to all users
                        Set<String> receipients = (Set<String>) message.getData();
                        broadcastMessage(receipients, new Message("MESSAGE DELIVERY", message.getExtraData()));
                    } else if (message.getMessage().equals("DISCONNECT")) {
                        break;
                    } else if (message.getMessage().equals("ACTIVE USERS")) {
                        // Respond all the active users
                        Set<String> usernames = new HashSet<>();

                        for (String aUsername : users.keySet()) {
                            if (users.get(aUsername).checkBox.isSelected()) {
                                usernames.add(aUsername);
                            }
                        }

                        oos.writeObject(new Message("ACTIVE USERS", usernames));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Disconnect this user, tell everyone this user got disconnected
            user.checkBox.setSelected(false);
            usersPanel.updateUI();
            broadcastMessage(users.keySet(), new Message("DISCONNECTED USER", username));
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

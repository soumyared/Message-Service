# Message-Service
Client

Startup:

1. Prompt the user to input a username.
2. Connect to the server over a socket and register the username.
a. When the client is connected, the user should be notified of the active connection.
b. If the provided username is already in use, the client should disconnect and prompt the user to input another username.
3. Proceed to send and receive messages until manually killed by the user.

Sending Messages:

1. The client will present the list of usernames received by the server to the user.
2. The user will be prompted to select from one of the three messaging options listed above.
3. The user will be prompted to select their intended recipient(s).
4. After the recipients are selected, the user should be prompted to input a brief text message.
5. The client will upload the text message to the server.
6. Return to Sending Messages: Step 1 until manually disconnected by the user.
Receiving Messages:
1. When connected to the server, the client will listen for incoming text messages.
2. Upon receipt of a text message, the client will print the text message to the GUI. The text message should indicate from which username the message was received.
3. Return to Receiving Messages: Step 1 until manually disconnected by the user.

Server:

The server should support three concurrently connected clients. A cumulative log of all previously used usernames should be maintained by the server and presented on the server’s GUI. The server should indicate which of those usernames (if any) represent currently connected clients. The server will execute the following sequence of steps:
1. Startup and listen for incoming connections.
2. Print that a client has connected, log the client’s username, and:
a. If the client username is available (e.g., not currently being used by another client), fork a thread to handle that client. Or,
b. If the username is in use, reject the connection from that client.
3. When a client connection is accepted, the server should provide the log of usernames to the client.
4. When a text message is received from a client, the server should:
a. Forward that message to the intended recipients, if those recipients are connected. Or,
b. Indicate the present recipients are not presently connected to the server.
5. Begin at step 2 until the process is killed by the user.

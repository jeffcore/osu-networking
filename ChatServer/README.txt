Jeff Rix
rixj@onid.oregonstate.edu

Files:
--------------------------------------------------------------------------------
Server: ChatServe.java
Client: chatclient.py 

Compiling and Running Programs
--------------------------------------------------------------------------------
Server:  ChatServe.java
Compile Program
$ javac Chatserve.java
Run Program
$ java ChatServe 30020

usage: java ChatServe port_number

Client: chatclient.py
Run Program
$ python3 chatclient.py localhost 30020

usage: python3 chatclient.py hostname port_number

Control Programs
--------------------------------------------------------------------------------
ChatServe.java
Server will start automatically and wait for clients. When a client connects a 
message will appear “Client Has Connected”. The server will wait for the 
client to type the first message. After the client message is received, the 
server can send a message back (500 or less characters including spaces). 
Simply type the message into the command line “server> “ and hit enter. 

quitting the chat with the client: type “\quit” at anytime, the server will 
continue to listen for new clients

chatclient.py
The program will automatically connect to the server specified in the command 
line args. After a connection is established, you will be prompted to enter a 
handle (one word up to 10 characters).  Then you will be prompted to enter 
your first message (500 or less characters including spaces). After typing 
your message hit enter to send it to the server. Then you will wait 
for a reply message from the server

quitting the chat with the server: type “\quit” at anytime, and you will be 
disconnected from the server.


Testing
---------------------------------------------------------------------------------
I tested the programs using flip1.engr.oregonstate.edu



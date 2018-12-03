#
# CS362-400-W15
# Project 1
# Jeff Rix - rixj@onid.oregonstate.edu
#
# client.py
# Python 3
# Creates the client side of a client/server chat application
# Command Line Usage:  python3 client.py server_name server_port
#
# references
#
# https://docs.python.org/3.2/library/socket.html
# https://docs.python.org/3/library/sys.html?highlight=argv#sys.argv
#   used the above two links for general knowledge on of sockets programming
# https://norwied.wordpress.com/2012/04/17/how-to-connect-a-python-client-to-java-server-with-tcp-sockets/
#   used for main guideline for setting up a python client to java server application
# http://stackoverflow.com/questions/606191/convert-bytes-to-a-python-string
#   used to figure out how to convert bytes to string in python 3
#

__author__ = 'jeffr'

from socket import *
import sys

# main function or app
# param:   argv   the command line arguments
def main(argv):
    # process command line arguments - check for two arguments
    server_name, server_port = get_arguments(argv)
    # create socket
    client_socket = initiate_contact(server_name, server_port)
    # get handle of user
    handle = get_handle()
    # while loop to create state of chat program
    while 1:
        # client send message to server
        if send_message(client_socket, handle):
            break
        # receive message from client
        if retrieve_message(client_socket):
            break
    # close socket chat is over
    client_socket.close()


# function to process arguments
# param:   argv   the command line arguments
# return:  server socket name - string
# return:  server socket port - int
def get_arguments(argv):
    # process command line arguments - check for two arguments
    if len(argv) < 2:
        print('You forgot a command line argument. Please try again.')
        print('usage: python3 client.py server_name server_port')
        print('example: python3 client.py localhost 30200')
        sys.exit(1)
    # found two CL args - assign them to variables
    return argv[0], int(argv[1])

# function that initiates connection to server
# param:  server socket name - string
# param:  server socket port - int
# return:  client socket connection - socket
def initiate_contact(server_name, server_port):
    # create socket
    try:
        client_socket = socket(AF_INET, SOCK_STREAM)
    except error as msg:
        client_socket = None
    # connect to socket
    try:
        client_socket.connect((server_name, server_port))
    except error as msg:
        client_socket.close()
        client_socket = None
    # if there was an error connection to socket display error text
    if client_socket is None:
        print('Unable to connect to chat server. Please restart program.')
        sys.exit(1)
    return client_socket

# function that get the clients handle from prompt
# return: the handle
def get_handle():
    # get handle of user
    handle_not_valid = True
    while handle_not_valid:
        handle = input('What is your handle (one word name up to 10 characters): ')
        handle = handle.strip()
        # make sure handle if valid
        if len(handle) <= 0:
            print('Error: you didn\'t enter a handle')
        elif len(handle) > 10:
            print('Error: your handle is longer than 10 characters.')
        elif ' ' in handle:
            print('Error: you entered multiple words.')
        else:
            handle_not_valid = False
    return handle

# function that gets message from prompt and sends it to the server
# param:  the client socket connection
# param:  the clients handle
# return: killed_socket  - if socket was killed by client
def send_message(client_socket, handle):
    killed_socket = False
    # while loop to check for valid message length
    while 1:
        sentence = input(handle + '> ')
        if 0 < len(sentence) <= 500:
            break
        else:
            print('Message Was Not Sent Error: The message was too long or short.')
            print('It was ' + str(len(sentence)) + ' characters long and should be 1 to 500 characters in length.')
    # if message length is good send message to server
    client_socket.sendall(bytes(handle + '> ' + sentence + '\n', 'utf-8'))

    # check if client quit chat, then close connection
    if '\quit' in sentence:
        print('You left the chat.')
        print('Chat Server Closed')
        killed_socket = True
    else:
        print('waiting for server to reply...')
    return killed_socket

# function retrieved message from server
# param:  the client socket connection
# return: killed_socket  - if socket was killed by client
def retrieve_message(client_socket):
    killed_socket = False
    # wait for server to send response and save it to variable
    modified_sentence = client_socket.recv(1024)
    # decode reply
    reply = modified_sentence.decode('utf-8')
    # check if server quit chat
    if '\quit' in reply:
        print('Server Left Chat.')
        print('Chat Server Closed')
        killed_socket = True
    else:
        sys.stdout.write(reply)
    return killed_socket


if __name__ == "__main__":
    main(sys.argv[1:])
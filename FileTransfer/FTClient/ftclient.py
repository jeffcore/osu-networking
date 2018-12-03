#
# ftclient.py
# Python 3
# #
# CS362-400-W15
# Project 2
# Jeff Rix - rixj@onid.oregonstate.edu
#
# Usage: python3 ftclient.py <server host> <server port> <command> <data port>
#
#   Options  one of the following is required
#   -l, --list            list of files in directory
#   -g FILENAME, --get=FILENAME
#                          get file from server
#
#
# references
# Parsing CL args
#  http://www.alexonlinux.com/pythons-optparse-for-human-beings
#  https://docs.python.org/2/library/optparse.html
#  https://docs.python.org/3.2/library/socket.html
#  https://docs.python.org/3/library/sys.html?highlight=argv#sys.argv
# File transfer help
#  http://stackoverflow.com/questions/6836831/python-socket-file-transfer

from socket import *
import optparse
import sys
import os.path
BUFFER_SIZE = 1024


# main function or app
# param:   argv   the command line arguments
def main():
    # process command line arguments - check for two arguments
    server_name, server_port, data_port, show_list, filename, command = get_arguments()

    # create socket
    client_socket = initiate_contact(server_name, server_port)

    # client send message to server
    send_message(client_socket, gethostname() + ':' + command)

    # process command request actions from server
    handle_command(client_socket, data_port, show_list, filename)

    client_socket.close()


# function to process arguments
# param:   argv   the command line arguments
# return:  server socket name - string
# return:  server socket port - int
# return:  data port - int
# return:  show list
# return:  file name
# return:  command
def get_arguments():
    parser = optparse.OptionParser()
    parser.add_option('-l', '--list', help='list of files in directory', dest='show_list', default=False,
                      action='store_true')
    parser.add_option('-g', '--get', dest='filename', help='get file from server')

    (opts, args) = parser.parse_args()

    # print(opts)
    # print(opts.show_list)
    # print(args)

    # check for command option
    if not opts.show_list and not opts.filename:
        print('Mandatory command option is missing.\n')
        print_arg_usage()
        sys.exit(1)
    #check for three non option arguments
    if len(args) < 3:
        print('You forgot a command line argument.\n')
        print_arg_usage()
        sys.exit(1)

    # get data
    try:
        server_name = args[0]
        server_port = int(args[1])
        data_port = int(args[2])
    except ValueError:
        print('Your command line arguments are all messed up.\n')
        print_arg_usage()
        sys.exit(1)

    show_list = opts.show_list
    filename = opts.filename

    # check for option command
    if opts.show_list:
        command = str(data_port) + ':-l:'
    elif opts.filename:
        command = str(data_port) + ':-g:' + opts.filename

    # return all args data
    return server_name, server_port, data_port, show_list, filename, command

def print_arg_usage():
    print('Usage: python3 ftclient.py  <server_name> <command_port> <command options> <data_port>')
    print('Command Options (please only use one):')
    print('  -l\t\tshow list of files')
    print('  -g <FILENAME> \tget name of file')
    print('\nexample: python3 ftclient.py localhost 30200 -l 20023')


# function that initiates connection to server
# param:  server socket name - string
# param:  server socket port - int
# return:  client socket connection - socket
def initiate_contact(server_name, server_port):
    # create socket
    try:
        client_socket = socket(AF_INET, SOCK_STREAM)
    except error:
        client_socket = None
    # connect to socket
    try:
        client_socket.connect((server_name, server_port))
    except error:
        client_socket.close()
        client_socket = None
    # if there was an error connection to socket display error text
    if client_socket is None:
        print('Unable to connect to chat server. Please restart program.')
        sys.exit(1)
    print('Connected to server')
    return client_socket


# function that gets message from prompt and sends it to the server
# param:  the client socket connection
# param:  the command    the command to send to the server
def send_message(client_socket, command):
    # if message length is good send message to server
    try:
        client_socket.sendall(bytes(command + '\n', 'utf-8'))
    except error:
        print('Error sending command to server. Client shut down.')
        sys.exit(1)


# function handles the response from the server after command is sent
# param:  client_socket the client socket connection for commands     socket connection
# param:  data_port     data port for data server hosted by client    int
# param:  show_list     command to show list of files                 boolean
# param:  filename      name of file client is requesting from server string
def handle_command(client_socket, data_port, show_list, filename):
    # start data socket and listen for incoming data
    try:
        data_socket = socket(AF_INET, SOCK_STREAM)
        data_socket.bind((gethostname(),  data_port))
        data_socket.listen(1)
    except IOError:
        print('Error creating data port')
        sys.exit(1)
    # if list command accept connection from
    if show_list:
        if retrieve_message(client_socket):
            # accept connections from outside
            try:
                (client_stream, address) = data_socket.accept()
            except IOError:
                print('Error accepting data socket')
                sys.exit(1)
            retrieve_message(client_stream)
            # close socket chat is over
            client_stream.close()
    else:
        if retrieve_message(client_socket):
            # accept connections from outside
            try:
                (client_stream, address) = data_socket.accept()
                msg = ""
                # read the file contents in
                while 1:
                    chunk = client_stream.recv(BUFFER_SIZE).decode('utf-8', 'replace')
                    if not chunk:
                        break
                    msg = msg + chunk
            except IOError:
                print('Error accepting data socket')
                sys.exit(1)
            try:
                f = open(rename_file_if_exists(filename, 1), 'w')
                f.write(msg)
                f.close()
                # print(msg)
            except error:
                print("there is an error opening file")

            print('File transfer complete')

            # close socket
            client_stream.close()

    # close data socket
    data_socket.close()


# function checks if file exisits
# param:  the client socket connection
# return: success  - if command was succesfully processed by server
def rename_file_if_exists(filename, i):
    if os.path.exists(filename):
        filename = str(i) + '-' + filename
        i += 1
        filename = rename_file_if_exists(filename, i)
    else:
        filename

    return filename


# function retrieved message from server
# param:  the client socket connection
# return: success  - if command was succesfully processed by server
def retrieve_message(client_socket):
    success = True
    # wait for server to send response and save it to variable
    modified_sentence = client_socket.recv(BUFFER_SIZE)
    # decode reply
    reply = modified_sentence.decode('utf-8')
    # check if server quit chat
    if 'FNF' in reply:
        print('Server says FILE NOT FOUND.')
        success = False
    elif 'CLOSED' in reply:
        print('Server error processing command.')
        success = False
    else:
        sys.stdout.write(reply)
    return success


if __name__ == "__main__":
    main()
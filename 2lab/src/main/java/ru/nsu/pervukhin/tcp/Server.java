package ru.nsu.pervukhin.tcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import static ru.nsu.pervukhin.tcp.ConstClass.*;

public class Server {
    private static LinkedList<ClientList> clientList = new LinkedList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[PORT_VAL_FOR_SERVER]));
            Socket socket = serverSocket.accept();
            ClientList clientHandler = new ClientList(socket);
            clientList.add(clientHandler);
            clientHandler.start();
            clientList.removeIf(ClientList::getFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

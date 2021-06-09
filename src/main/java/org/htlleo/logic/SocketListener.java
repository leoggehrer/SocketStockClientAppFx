package org.htlleo.logic;

import org.htlleo.models.Message;
import org.htlleo.models.MessageDistributor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class SocketListener extends Thread {
    private static final String DisconnectedCommand = "disconnected";
    private UUID id;
    private Socket socket;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    public SocketListener(Socket socket) {
        if (socket == null)
            throw new IllegalArgumentException("socket");

        this.socket = socket;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDaemon(true);
        id = UUID.randomUUID();
    }

    @Override
    public void run() {
        boolean error = false;
        String command = "";

        while (error == false && command.equals(DisconnectedCommand) == false) {
           try {
               Message message = (Message)ois.readObject();

               MessageDistributor.getInstance().addMessage(message);
               System.out.printf("%s\n", message.toString());
               command = message.getCommand();
            } catch (IOException e) {
               error = true;
               e.printStackTrace();
            } catch (ClassNotFoundException e) {
               error = true;
               e.printStackTrace();
            }
        }
        try {
            oos.close();
            oos = null;
            ois.close();
            ois = null;
            socket.close();
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeMessage(Message message) {
        if (message == null)
            throw new IllegalArgumentException("message");

        if (oos != null) {
            try {
                message.setId(id);
                oos.writeObject(message);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

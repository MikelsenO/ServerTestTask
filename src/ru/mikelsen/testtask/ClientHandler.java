package ru.mikelsen.testtask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        start();
    }

    @Override
    public void run() {
        try {
            DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
            while (!isInterrupted()) {
                byte[] message = new byte[8];
                inputStream.readFully(message);

                short ll = (short) ((Byte.toUnsignedInt(message[0]) << 8) | Byte.toUnsignedInt(message[1])); // длина сообщения
                short cc = (short) ((Byte.toUnsignedInt(message[2]) << 8) | Byte.toUnsignedInt(message[3])); // код комманды
                short pp = (short) ((Byte.toUnsignedInt(message[4]) << 8) | Byte.toUnsignedInt(message[5])); // первый аргумент
                short vv = (short) ((Byte.toUnsignedInt(message[6]) << 8) | Byte.toUnsignedInt(message[7])); // второй аргумент

                System.out.println("Message recieved: "
                        + Integer.toHexString(ll).toUpperCase() + " "
                        + Integer.toHexString(cc).toUpperCase() + " "
                        + Integer.toHexString(pp).toUpperCase() + " "
                        + Integer.toHexString(vv).toUpperCase() + " ");


                String command = Commands.commands.get(cc);
                int result = (int) Calculation.class.getMethod(command, int.class, int.class).invoke(null, pp, vv);

                message[4] = (byte) (result >> 24);
                message[5] = (byte) (result >> 16);
                message[6] = (byte) (result >> 8);
                message[7] = (byte) result;

                outputStream.write(message);
                outputStream.flush();
                System.out.println("Message sent: "
                        + Integer.toHexString(ll).toUpperCase() + " "
                        + Integer.toHexString(cc).toUpperCase() + " "
                        + Integer.toHexString(((Byte.toUnsignedInt(message[4]) << 8) | Byte.toUnsignedInt(message[5]))).toUpperCase() + " "
                        + Integer.toHexString(((Byte.toUnsignedInt(message[6]) << 8) | Byte.toUnsignedInt(message[7]))).toUpperCase() + " ");
            }
        } catch (EOFException eofe) {
            interrupt();
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            clientSocket.close();
            System.out.println("Client disconnected");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}

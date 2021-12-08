package ru.nsu.pervukhin.tcp;

import com.google.common.hash.Hashing;

import java.io.*;
import java.net.Socket;

import static ru.nsu.pervukhin.tcp.ConstClass.*;

public class ClientList extends Thread {
    private static Socket socket;
    private static BufferedWriter outputMail;
    private static DataInputStream input;
    private static boolean flag = false;
    private static TimeChecker timeChecker;

    public ClientList (Socket inputSocket) throws IOException {
        socket = inputSocket;
        outputMail = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.timeChecker = new TimeChecker();
    }

    public boolean getFlag () {
        return flag;
    }

    @Override
    public void run () {
        try {
            System.out.println("Server start");
            File inputFile = new File(input.readUTF());

            outputMail.write("The name of file is " + inputFile.getName() + "\n");
            outputMail.flush();

            long firstFileLength;
            outputMail.write("The file length " + (firstFileLength = Long.parseLong(input.readUTF())) + " byte" + " \n");
            outputMail.flush();

            File directory = new File("uploads/");
            if (!(directory.exists())) {
                directory.mkdir();
            }
            timeChecker.start();
            FileOutputStream outFile = new FileOutputStream("uploads/" + inputFile.getName().trim());

            byte[]buf = new byte[CHUNK_LENGTH];


            long leftBytes = firstFileLength;
            String sha256hex;
            String sha256hexHelper;
            int nextChunk;
            int count;

            while (leftBytes > 0) {

                nextChunk = (int) Math.min(leftBytes, CHUNK_LENGTH);

                sha256hexHelper = input.readUTF();
                count = input.read(buf, 0, nextChunk);
                sha256hex = Hashing.sha256().hashBytes(buf).toString();
                if (sha256hex.equals(sha256hexHelper)) {
                    outFile.write(buf, 0, count);
                    leftBytes -= count;
                    timeChecker.checkSpeed(count);
                } else {
                    System.err.println("Problems with file");
                    closeConnection();
                }
            }


            timeChecker.finish();
            File secondFile = new File("uploads/" + inputFile.getName().trim());
            if (firstFileLength == secondFile.length()) {
                outputMail.write("File upload successfully\n");
            } else {
                outputMail.write("File wasn't upload\n");
            }
            outputMail.flush();

            outFile.close();
            flag = true;
            closeConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection () throws IOException {
        System.out.println("Server close");
        outputMail.close();
        input.close();
        socket.close();
    }
}

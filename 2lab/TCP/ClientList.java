package TCP;

import java.io.*;
import java.net.Socket;

import static TCP.ConstClass.*;

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
            byte[]buf = new byte[ChunkLength];
            int count;
            while ((count = input.read(buf)) > 0) {
                if (buf[count - 1 ] == -1) {
                    outFile.write(buf, 0, count - 1);
                    outFile.flush();
                    break;
                }
                outFile.write(buf, 0, count );
                outFile.flush();
                timeChecker.checkSpeed(count);
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
            System.out.println("Server close");
            outputMail.close();
            input.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

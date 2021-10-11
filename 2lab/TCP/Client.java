package TCP;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import static TCP.ConstClass.*;

public class Client {

    private static Socket socket;
    private static BufferedReader inputMail;
    private static DataOutputStream output;

    public static void main(String[] args) {
        try {
            socket = new Socket(args[IPAddressForClient], Integer.parseInt(args[PortValForClient]));

            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            inputMail = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            File file = new File(args[FileIndex]);

            output.writeUTF(file.getName());
            output.flush();
            System.out.println(inputMail.readLine());

            output.writeUTF(Objects.toString(file.length()));
            output.flush();
            System.out.println(inputMail.readLine());


            FileInputStream fis = new FileInputStream(file);
            byte[]buf = new byte[ChunkLength];
            int count;
            while((count = fis.read(buf)) > 0) {
                output.write(buf,0, count);
                output.flush();
            }
            output.write(-1);
            output.flush();

            System.out.println(inputMail.readLine());

            inputMail.close();
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

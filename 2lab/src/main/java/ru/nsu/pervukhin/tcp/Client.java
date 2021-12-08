package ru.nsu.pervukhin.tcp;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import static ru.nsu.pervukhin.tcp.ConstClass.*;
import com.google.common.hash.*;
public class Client {
    private static Socket socket;
    private static BufferedReader inputMail;
    private static DataOutputStream output;

    public static void main(String[] args) {
        try {


            socket = new Socket(args[IP_ADDRESS_FOR_CLIENT], Integer.parseInt(args[PORT_VAL_FOR_CLIENT]));

            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            inputMail = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            File file = new File(args[FILE_INDEX]);

            if (!(file.exists())) {
                System.err.println("File not found");
            }

            output.writeUTF(file.getName());
            output.flush();

            output.flush();
            System.out.println(inputMail.readLine());


            output.writeUTF(Objects.toString(file.length()));
            output.flush();
            System.out.println(inputMail.readLine());


            FileInputStream fis = new FileInputStream(file);
            byte[]buf = new byte[CHUNK_LENGTH];
            int count;

            String sha256hex;

            while((count = fis.read(buf)) > 0) {
                sha256hex = Hashing.sha256().hashBytes(buf).toString();
                output.writeUTF(sha256hex);
                output.flush();
                output.write(buf,0, count);
                output.flush();
            }

            System.out.println(inputMail.readLine());

            inputMail.close();
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
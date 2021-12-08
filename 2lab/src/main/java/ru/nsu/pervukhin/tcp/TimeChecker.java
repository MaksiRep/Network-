package ru.nsu.pervukhin.tcp;
import static ru.nsu.pervukhin.tcp.ConstClass.*;

public class TimeChecker {

    private long start;
    private long checkpointTime;
    private long byteRead;
    private long checkpointByte;

    private int currentSpeed;
    private int sessionSpeed;

    public void start() {
        this.checkpointTime = 0;
        this.checkpointByte = 0;
        this.byteRead = 0;
        this.start = System.currentTimeMillis();
        this.checkpointTime = this.start;
    }

    void checkSpeed(int countReadBytes) {
        byteRead += countReadBytes;
        long currentTime = System.currentTimeMillis();

        if (currentTime - checkpointTime >= PRINT_TIME) {
            calculateSpeed(currentTime);
            printSpeed();
        }
    }

    private void calculateSpeed(long currentTime) {
        currentSpeed = (int) ((byteRead - checkpointByte) / (currentTime - checkpointTime ));
        sessionSpeed = (int) (byteRead / (currentTime - start ));

        this.checkpointTime = currentTime;
        this.checkpointByte = byteRead;
    }

    public void printSpeed() {
        System.out.println("''''''''''''''''''''''''''''''''''''''''\"" +
                "\nCurrent Speed:\t" + currentSpeed + " KB/Sec" +
                "\nSession Speed:\t" + sessionSpeed + " KB/Sec" +
                "\n''''''''''''''''''''''''''''''''''''''''");
    }

    public void finish() {
        calculateSpeed(System.currentTimeMillis());
        printSpeed();
    }

}

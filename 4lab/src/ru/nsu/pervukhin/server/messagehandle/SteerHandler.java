package ru.nsu.pervukhin.server.messagehandle;

import me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import ru.nsu.pervukhin.game.GameController;
import ru.nsu.pervukhin.messagemanagement.MessageHandler;
import ru.nsu.pervukhin.messagemanagement.MessageManager;
import ru.nsu.pervukhin.server.Server;

public class SteerHandler implements MessageHandler {
    private Server server;
    private MessageManager messageManager;
    private GameController gameController;

    public SteerHandler(GameController gameController, Server server, MessageManager messageManager) {
        this.gameController = gameController;
        this.server = server;
        this.messageManager = messageManager;
    }

    public void handleMessage(GameMessage message, String senderAddress, int senderPort) {
//        System.out.println("received steer");
        server.updateActivePlayers(message.getSenderId());
        server.addSteerMsg(message);
        GameMessage ack = GameMessage.newBuilder()
                .setMsgSeq(message.getMsgSeq())
                .setAck(GameMessage.AckMsg.newBuilder()
                        .build())
                .setSenderId(gameController.getMasterId())
                .build();
        messageManager.sendMessage(ack, senderAddress, senderPort);
    }
}

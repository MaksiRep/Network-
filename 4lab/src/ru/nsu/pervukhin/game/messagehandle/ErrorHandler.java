package ru.nsu.pervukhin.game.messagehandle;

import me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import ru.nsu.pervukhin.game.GameController;
import ru.nsu.pervukhin.messagemanagement.MessageHandler;
import ru.nsu.pervukhin.messagemanagement.MessageManager;

public class ErrorHandler implements MessageHandler {
    private GameController gameController;
    private MessageManager messageManager;

    public ErrorHandler(GameController gameController, MessageManager messageManager) {
        this.gameController = gameController;
        this.messageManager = messageManager;
    }

    public void handleMessage(GameMessage message, String senderAddress, int senderPort) {
        gameController.updateLastMsgFromMasterTime();

        gameController.getGameView().showErrorMessage(message.getError().getErrorMessage());
        GameMessage ack = GameMessage.newBuilder()
                .setMsgSeq(message.getMsgSeq())
                .setAck(GameMessage.AckMsg.newBuilder()
                        .build())
                .setSenderId(gameController.getPlayerId())
                .build();
        messageManager.sendMessage(ack, senderAddress, senderPort);
        gameController.closeGameView();
    }
}

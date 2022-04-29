package com.api.liargame.domain;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameTimer implements Runnable {

    Timer timer;
    Task task;
    long period;
    long delay;

    @Getter
    @Setter
    public class Task extends TimerTask {
        private SimpMessagingTemplate webSocket;
        private GameRoom gameRoom;
        private Integer time;

        public void run() {
            webSocket.convertAndSend("/sub/game/" + gameRoom.getRoomId() + "/countdown",
                    "gameId: " + gameRoom.getRoomId() + " - " + time--);
        }
    }

    public GameTimer() {
        timer = new Timer();
        task = new Task();

    }

    public void run() {
        timer.scheduleAtFixedRate(task, delay, period);
        try {
            Thread.sleep(delay + ((task.getGameRoom().getSetting().getTimeLimit()-1) * 1000));
        } catch (InterruptedException exc) {
            timer.cancel();
        }
        timer.cancel();
    }

    public void setWebSocket(SimpMessagingTemplate webSocket) {
        task.setWebSocket(webSocket);
    }

    public void setGameRoom(GameRoom gameRoom) {
        task.setGameRoom(gameRoom);
        task.setTime(task.getGameRoom().getSetting().getTimeLimit());
    }
}

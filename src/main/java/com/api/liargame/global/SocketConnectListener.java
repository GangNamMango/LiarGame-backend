package com.api.liargame.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class SocketConnectListener {

  @EventListener
  public void handleSessionConnect(SessionConnectEvent event) {
    log.info("🟢Socket connect");
  }

  @EventListener
  public void handleSessionDisConnect(SessionDisconnectEvent event) {
    log.info("🔴Socket disconnect");
  }
}

package com.api.liargame.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Slf4j
@Component
public class SocketConnectListener {

  @EventListener
  public void handleSessionConnect(SessionConnectEvent event) {
    log.info("🛑Socket connect");
  }

}

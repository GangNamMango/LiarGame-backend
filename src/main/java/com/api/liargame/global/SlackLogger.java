package com.api.liargame.global;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SlackLogger {

  @Value("${SLACK_URL}")
  private String url;
  private static final String iconEmoji = ":liargame:";

  @Async
  public void send(String text) {
    log.info("Logging to Slack");
    RestTemplate restTemplate = new RestTemplate();

    Map<String,Object> request = new HashMap<>();
    request.put("username", "라이어게임-알리미");
    request.put("text", text);
    request.put("icon_emoji",iconEmoji);

    HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);

    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}

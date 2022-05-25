package com.api.liargame.global;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SlackLogger {

  @Value("${SLACK_URL}")
  private String url;
  private static final String iconEmoji = ":liargame:";

  public void send(String text) {
    RestTemplate restTemplate = new RestTemplate();

    Map<String,Object> request = new HashMap<>();
    request.put("username", "라이어게임-알리미");
    request.put("text", text);
    request.put("icon_emoji",iconEmoji);

    HttpEntity<Map<String,Object>> entity = new HttpEntity<>(request);

    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}

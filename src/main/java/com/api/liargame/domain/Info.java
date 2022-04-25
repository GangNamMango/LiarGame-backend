package com.api.liargame.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Info {

  private final User liar;
  private final String topic;
  private final String word;

  public static Info create(User user, String topic, String word) {
    return new Info(user, topic, word);
  }
}

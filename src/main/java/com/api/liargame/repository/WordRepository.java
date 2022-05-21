package com.api.liargame.repository;

import java.util.List;

public interface WordRepository {

  String findWordByTopic(String topic);

  List<String> findTopics();

  String resetTopic(String topic);
}

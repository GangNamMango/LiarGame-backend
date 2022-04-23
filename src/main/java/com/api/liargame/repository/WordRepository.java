package com.api.liargame.repository;

import java.util.List;

public interface WordRepository {

  String findWordByTopic(String topic);

  String findWordByRandomTopic();

  List<String> findTopics();
}

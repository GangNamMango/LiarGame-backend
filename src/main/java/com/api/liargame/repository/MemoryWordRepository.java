package com.api.liargame.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryWordRepository implements WordRepository {

  public static final String WORD_DB_PATH = "json/word.json";
  private final Map<String, List<String>> db;

  
  public MemoryWordRepository() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(WORD_DB_PATH);

    ObjectMapper objectMapper = new ObjectMapper();
    db = objectMapper.readValue(inputStream, Map.class);
  }

  @Override
  public String findWordByTopic(String topic) {
    if (!db.containsKey(topic))
      throw new IllegalStateException("존재하지 않는 주제입니다.");

    List<String> words = db.get(topic);

    Random random = new Random();
    int randomIndex = random.nextInt(words.size());

    return words.get(randomIndex);
  }

  @Override
  public String resetTopic(String topic) {
    if (topic.equals("랜덤"))
      return findWordByRandomTopic();
    return topic;
  }

  private String findWordByRandomTopic() {
    List<String> topics = new ArrayList<>(db.keySet());
    int randomIndex = new Random().nextInt(db.keySet().size());

    return topics.get(randomIndex);
  }

  @Override
  public List<String> findTopics() {
    return new ArrayList<>(db.keySet());
  }

  public List<String> findAllWordsByTopic(String topic) {
    if (!db.containsKey(topic))
      throw new IllegalStateException("존재하지 않는 주제입니다.");

    return db.get(topic);
  }
}

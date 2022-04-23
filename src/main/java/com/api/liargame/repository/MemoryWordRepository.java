package com.api.liargame.repository;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryWordRepository implements WordRepository {

  private final Map<String, List<String>> db;

  public MemoryWordRepository() throws IOException {
    ClassPathResource classPathResource = new ClassPathResource("json/word.json");
    Path path = Paths.get(classPathResource.getURI());
    Reader reader = new FileReader(path.toString());

    db = new Gson().fromJson(reader, Map.class);
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
  public String findWordByRandomTopic() {
    List<String> topics = new ArrayList<>(db.keySet());
    int randomIndex = new Random().nextInt(db.keySet().size());

    return findWordByTopic(topics.get(randomIndex));
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

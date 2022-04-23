package com.api.liargame.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemoryWordRepositoryTest {

  MemoryWordRepository wordRepository = new MemoryWordRepository();

  MemoryWordRepositoryTest() throws IOException {}

  @Test
  @DisplayName("topic이 주어졌을 때 랜덤한 단어를 받을 수 있다.")
  void test() {
    String topic = "major";
    List<String> words = wordRepository.findAllWordsByTopic(topic);

    for (int i=0; i<10; i++) {
      String findWord = wordRepository.findWordByTopic(topic);
      assertThat(words).contains(findWord);
    }
  }

  @Test
  @DisplayName("존재하지 않은 주제로 검색할 시 예외가 발생해야 한다.")
  void test_fail() {

    assertThrows(IllegalStateException.class ,() -> wordRepository.findWordByTopic("xxxxxxxx"));
  }

}

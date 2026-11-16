package org.wise.vle.domain.chatbot;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;

public class ChatMessageTest {

  @Test
  public void initialState_hasExpectedDefaults() {
    ChatMessage message = new ChatMessage();
    assertNull(message.getId());
    assertNull(message.getChat());
    assertNull(message.getRole());
    assertNull(message.getContent());
    assertNull(message.getTimestamp());
    assertNull(message.getNodeId());
    assertNull(message.getChatId());
    assertEquals(ChatMessage.class, message.getObjectClass());
  }

  @Test
  public void gettersAndSetters_workAsExpected() {
    ChatMessage message = new ChatMessage();
    Long id = 99L;
    Chat chat = new Chat();
    chat.setId(42L);
    String role = "user";
    String content = "How does photosynthesis work?";
    Timestamp timestamp = Timestamp.from(Instant.now());
    String nodeId = "node_1";
    Long chatId = 42L;

    message.setId(id);
    message.setChat(chat);
    message.setRole(role);
    message.setContent(content);
    message.setTimestamp(timestamp);
    message.setNodeId(nodeId);
    message.setChatId(chatId);

    assertEquals(id, message.getId());
    assertSame(chat, message.getChat());
    assertEquals(role, message.getRole());
    assertEquals(content, message.getContent());
    assertEquals(timestamp, message.getTimestamp());
    assertEquals(nodeId, message.getNodeId());
    assertEquals(chatId, message.getChatId());
  }

  @Test
  public void convertToClientChatMessage_withNonNullChat_setsChatId() {
    ChatMessage message = new ChatMessage();
    Chat chat = new Chat();
    chat.setId(55L);
    message.setChat(chat);

    message.convertToClientChatMessage();

    assertEquals(55L, message.getChatId());
  }

  @Test
  public void convertToClientChatMessage_withNullChat_doesNotThrow() {
    ChatMessage message = new ChatMessage();
    assertDoesNotThrow(message::convertToClientChatMessage);
    assertNull(message.getChatId());
  }
}

package org.wise.vle.domain.chatbot;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;

public class ChatTest {

  @Test
  public void initialState_hasExpectedDefaults() {
    Chat chat = new Chat();
    assertNull(chat.getId());
    assertFalse(chat.isDeleted());
    assertNotNull(chat.getMessages());
    assertTrue(chat.getMessages().isEmpty());
    assertEquals(Chat.class, chat.getObjectClass());
  }

  @Test
  public void gettersAndSetters_workAsExpected() {
    Chat chat = new Chat();
    Long id = 42L;
    String title = "Sample Chat Conversation";
    Timestamp now = Timestamp.from(Instant.now());
    Run run = new RunImpl();
    Workgroup workgroup = new WorkgroupImpl();

    chat.setId(id);
    chat.setTitle(title);
    chat.setCreatedAt(now);
    chat.setLastUpdated(now);
    chat.setDeleted(true);
    chat.setRun(run);
    chat.setWorkgroup(workgroup);
    chat.setRunId(10L);
    chat.setWorkgroupId(20L);

    assertEquals(id, chat.getId());
    assertEquals(title, chat.getTitle());
    assertEquals(now, chat.getCreatedAt());
    assertEquals(now, chat.getLastUpdated());
    assertTrue(chat.isDeleted());
    assertSame(run, chat.getRun());
    assertSame(workgroup, chat.getWorkgroup());
    assertEquals(10L, chat.getRunId());
    assertEquals(20L, chat.getWorkgroupId());
  }

  @Test
  public void addMessage_associatesMessageBidirectionally() {
    Chat chat = new Chat();
    ChatMessage message = new ChatMessage();

    chat.addMessage(message);

    assertEquals(1, chat.getMessages().size());
    assertTrue(chat.getMessages().contains(message));
    assertSame(chat, message.getChat());
  }

  @Test
  public void removeMessage_disassociatesMessageBidirectionally() {
    Chat chat = new Chat();
    ChatMessage message = new ChatMessage();
    chat.addMessage(message);

    chat.removeMessage(message);

    assertTrue(chat.getMessages().isEmpty());
    assertNull(message.getChat());
  }

  @Test
  public void convertToClientChat_setsRunIdAndWorkgroupId() {
    Chat chat = new Chat();
    Run run = new RunImpl();
    run.setId(101L);
    Workgroup workgroup = new WorkgroupImpl();
    workgroup.setId(202L);

    chat.setRun(run);
    chat.setWorkgroup(workgroup);

    chat.convertToClientChat();

    assertEquals(101L, chat.getRunId());
    assertEquals(202L, chat.getWorkgroupId());
  }
}

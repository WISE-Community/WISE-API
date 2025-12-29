package org.wise.portal.service.chatbot.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.wise.portal.service.chatbot.ChatbotService;
import org.wise.vle.domain.chatbot.Chat;
import org.wise.vle.domain.chatbot.ChatMessage;

/**
 * Implementation of ChatbotService for managing chatbot conversations
 * 
 * @author Hiroki Terashima
 */
@Service
public class ChatbotServiceImpl implements ChatbotService {

	@Override
	public List<Chat> getAllChats(Long runId, Long workgroupId) {
		List<Chat> chats = new ArrayList<>();

		// Dummy chat 1
		Chat chat1 = new Chat();
		chat1.setId(1766593333094L);
		chat1.setTitle("everest");
		chat1.setCreatedAt(Timestamp.from(Instant.parse("2025-12-24T16:22:13.094Z")));
		chat1.setLastUpdated(Timestamp.from(Instant.parse("2025-12-24T16:26:57.513Z")));

		chat1.addMessage(
		    createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		chat1.addMessage(createMessage("user", "how tall is everest?",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:07.027Z"))));
		chat1.addMessage(createMessage("assistant",
		    "Mount Everest's current official height is 8,848.86 meters (29,031.7 feet) above sea level.",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:07.572Z"))));
		chat1.addMessage(createMessage("user", "where is it?",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:57.020Z"))));
		chat1.addMessage(createMessage("assistant",
		    "Mount Everest is located on the border between Nepal and China (Tibet Autonomous Region).",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:57.513Z"))));

		// Dummy chat 2
		Chat chat2 = new Chat();
		chat2.setId(1766593571381L);
		chat2.setTitle("k2");
		chat2.setCreatedAt(Timestamp.from(Instant.parse("2025-12-24T16:26:11.381Z")));
		chat2.setLastUpdated(Timestamp.from(Instant.parse("2025-12-24T16:26:28.333Z")));

		chat2.addMessage(
		    createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		chat2.addMessage(createMessage("user", "how tall is m2?",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:16.485Z"))));
		chat2.addMessage(createMessage("assistant",
		    "The height of an M2 building can vary depending on the specific design and construction of the building. If you have a particular M2 building in mind, I can try to look up the information for you.",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:18.034Z"))));

		// Dummy chat 3
		Chat chat3 = new Chat();
		chat3.setId(1766593589760L);
		chat3.setTitle("berkeley");
		chat3.setCreatedAt(Timestamp.from(Instant.parse("2025-12-24T16:26:29.760Z")));
		chat3.setLastUpdated(Timestamp.from(Instant.parse("2025-12-24T16:26:41.855Z")));

		chat3.addMessage(
		    createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		chat3.addMessage(createMessage("user", "where is berkeley?",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:33.778Z"))));
		chat3.addMessage(createMessage("assistant",
		    "Berkeley is a city located in the state of California in the United States.",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:34.368Z"))));

		chats.add(chat1);
		chats.add(chat2);
		chats.add(chat3);

		return chats;
	}

	@Override
	public Chat getChat(Long runId, Long workgroupId, Long chatId) {
		Chat chat = new Chat();
		chat.setId(chatId);
		chat.setTitle("Sample Chat");
		chat.setCreatedAt(Timestamp.from(Instant.parse("2025-12-24T16:22:13.094Z")));
		chat.setLastUpdated(Timestamp.from(Instant.parse("2025-12-24T16:26:57.513Z")));

		chat.addMessage(
		    createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		chat.addMessage(
		    createMessage("user", "Hello!", Timestamp.from(Instant.parse("2025-12-24T16:26:07.027Z"))));
		chat.addMessage(createMessage("assistant", "Hello! How can I help you today?",
		    Timestamp.from(Instant.parse("2025-12-24T16:26:07.572Z"))));

		return chat;
	}

	@Override
	public Chat createChat(Long runId, Long workgroupId, Chat chat) {
		// Generate a dummy ID if not provided
		if (chat.getId() == null) {
			chat.setId(System.currentTimeMillis());
		}

		// Add timestamps if not provided
		Timestamp now = Timestamp.from(Instant.now());
		if (chat.getCreatedAt() == null) {
			chat.setCreatedAt(now);
		}
		if (chat.getLastUpdated() == null) {
			chat.setLastUpdated(now);
		}

		return chat;
	}

	@Override
	public Chat updateChat(Long runId, Long workgroupId, Long chatId, Chat chat) {
		chat.setId(chatId);
		chat.setLastUpdated(Timestamp.from(Instant.now()));

		return chat;
	}

	@Override
	public void deleteChat(Long runId, Long workgroupId, Long chatId) {
		// In a real implementation, this would delete from the database
		// For now, this is just a dummy implementation
	}

	/**
	 * Helper method to create a message object
	 * 
	 * @param role the role (system, user, assistant)
	 * @param content the message content
	 * @param timestamp the timestamp (optional)
	 * @return the message object
	 */
	private ChatMessage createMessage(String role, String content, Timestamp timestamp) {
		ChatMessage message = new ChatMessage();
		message.setRole(role);
		message.setContent(content);
		message.setTimestamp(timestamp);
		return message;
	}
}

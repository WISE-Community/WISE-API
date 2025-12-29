package org.wise.portal.service.chatbot.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.wise.portal.service.chatbot.ChatbotService;

/**
 * Implementation of ChatbotService for managing chatbot conversations
 * 
 * @author Hiroki Terashima
 */
@Service
public class ChatbotServiceImpl implements ChatbotService {

	@Override
	public List<Map<String, Object>> getAllChats(Long runId, Long workgroupId) {
		List<Map<String, Object>> chats = new ArrayList<>();

		// Dummy chat 1
		Map<String, Object> chat1 = new HashMap<>();
		chat1.put("id", "chat_1766593333094");
		chat1.put("title", "everest");
		chat1.put("createdAt", "2025-12-24T16:22:13.094Z");
		chat1.put("lastUpdated", "2025-12-24T16:26:57.513Z");

		List<Map<String, Object>> messages1 = new ArrayList<>();
		messages1
		    .add(createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		messages1.add(createMessage("user", "how tall is everest?", "2025-12-24T16:26:07.027Z"));
		messages1.add(createMessage("assistant",
		    "Mount Everest's current official height is 8,848.86 meters (29,031.7 feet) above sea level.",
		    "2025-12-24T16:26:07.572Z"));
		messages1.add(createMessage("user", "where is it?", "2025-12-24T16:26:57.020Z"));
		messages1.add(createMessage("assistant",
		    "Mount Everest is located on the border between Nepal and China (Tibet Autonomous Region).",
		    "2025-12-24T16:26:57.513Z"));
		chat1.put("messages", messages1);

		// Dummy chat 2
		Map<String, Object> chat2 = new HashMap<>();
		chat2.put("id", "chat_1766593571381");
		chat2.put("title", "k2");
		chat2.put("createdAt", "2025-12-24T16:26:11.381Z");
		chat2.put("lastUpdated", "2025-12-24T16:26:28.333Z");

		List<Map<String, Object>> messages2 = new ArrayList<>();
		messages2
		    .add(createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		messages2.add(createMessage("user", "how tall is m2?", "2025-12-24T16:26:16.485Z"));
		messages2.add(createMessage("assistant",
		    "The height of an M2 building can vary depending on the specific design and construction of the building. If you have a particular M2 building in mind, I can try to look up the information for you.",
		    "2025-12-24T16:26:18.034Z"));
		chat2.put("messages", messages2);

		// Dummy chat 3
		Map<String, Object> chat3 = new HashMap<>();
		chat3.put("id", "chat_1766593589760");
		chat3.put("title", "berkeley");
		chat3.put("createdAt", "2025-12-24T16:26:29.760Z");
		chat3.put("lastUpdated", "2025-12-24T16:26:41.855Z");

		List<Map<String, Object>> messages3 = new ArrayList<>();
		messages3
		    .add(createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		messages3.add(createMessage("user", "where is berkeley?", "2025-12-24T16:26:33.778Z"));
		messages3.add(createMessage("assistant",
		    "Berkeley is a city located in the state of California in the United States.",
		    "2025-12-24T16:26:34.368Z"));
		chat3.put("messages", messages3);

		chats.add(chat1);
		chats.add(chat2);
		chats.add(chat3);

		return chats;
	}

	@Override
	public Map<String, Object> getChat(Long runId, Long workgroupId, String chatId) {
		Map<String, Object> chat = new HashMap<>();
		chat.put("id", chatId);
		chat.put("title", "Sample Chat");
		chat.put("createdAt", "2025-12-24T16:22:13.094Z");
		chat.put("lastUpdated", "2025-12-24T16:26:57.513Z");

		List<Map<String, Object>> messages = new ArrayList<>();
		messages
		    .add(createMessage("system", "You are a helpful assistant. Be polite and concise.", null));
		messages.add(createMessage("user", "Hello!", "2025-12-24T16:26:07.027Z"));
		messages.add(
		    createMessage("assistant", "Hello! How can I help you today?", "2025-12-24T16:26:07.572Z"));
		chat.put("messages", messages);

		return chat;
	}

	@Override
	public Map<String, Object> createChat(Long runId, Long workgroupId,
	    Map<String, Object> chatData) {
		Map<String, Object> createdChat = new HashMap<>(chatData);

		// Generate a dummy ID if not provided
		if (!createdChat.containsKey("id")) {
			createdChat.put("id", "chat_" + System.currentTimeMillis());
		}

		// Add timestamps if not provided
		String now = java.time.Instant.now().toString();
		if (!createdChat.containsKey("createdAt")) {
			createdChat.put("createdAt", now);
		}
		if (!createdChat.containsKey("lastUpdated")) {
			createdChat.put("lastUpdated", now);
		}

		return createdChat;
	}

	@Override
	public Map<String, Object> updateChat(Long runId, Long workgroupId, String chatId,
	    Map<String, Object> chatData) {
		Map<String, Object> updatedChat = new HashMap<>(chatData);
		updatedChat.put("id", chatId);
		updatedChat.put("lastUpdated", java.time.Instant.now().toString());

		return updatedChat;
	}

	@Override
	public Map<String, String> deleteChat(Long runId, Long workgroupId, String chatId) {
		Map<String, String> response = new HashMap<>();
		response.put("message", "Chat deleted successfully");
		response.put("chatId", chatId);

		return response;
	}

	/**
	 * Helper method to create a message object
	 * 
	 * @param role the role (system, user, assistant)
	 * @param content the message content
	 * @param timestamp the timestamp (optional)
	 * @return the message map
	 */
	private Map<String, Object> createMessage(String role, String content, String timestamp) {
		Map<String, Object> message = new HashMap<>();
		message.put("role", role);
		message.put("content", content);
		if (timestamp != null) {
			message.put("timestamp", timestamp);
		}
		return message;
	}
}

package org.wise.portal.service.chatbot.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.chatbot.ChatDao;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
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

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ChatDao<Chat> chatDao;

	@Override
	@Transactional(readOnly = true)
	public List<Chat> getAllChats(Run run, Workgroup workgroup) {
		return chatDao.getChatsByRunAndWorkgroup(run, workgroup);

	}

	@Override
	@Transactional
	public Chat createChat(Run run, Workgroup workgroup, Chat chat) {
		chat.setRun(run);
		chat.setWorkgroup(workgroup);

		Timestamp now = Timestamp.from(Instant.now());
		if (chat.getCreatedAt() == null) {
			chat.setCreatedAt(now);
		}
		if (chat.getLastUpdated() == null) {
			chat.setLastUpdated(now);
		}
		if (chat.getMessages() != null) {
			chat.getMessages().forEach(message -> message.setChat(chat));
		}
		chatDao.save(chat);
		return chat;
	}

	@Override
	@Transactional
	public Chat updateChat(Run run, Workgroup workgroup, Long chatId, Chat updatedChat) {
		Chat existingChat = chatDao.getChatById(chatId);
		if (existingChat == null) {
			throw new IllegalArgumentException("Chat not found with id: " + chatId);
		}

		// Verify the chat belongs to the specified run and workgroup
		if (!existingChat.getRun().equals(run) || !existingChat.getWorkgroup().equals(workgroup)) {
			throw new IllegalArgumentException("Chat does not belong to the specified run and workgroup");
		}

		if (updatedChat.getTitle() != null) {
			existingChat.setTitle(updatedChat.getTitle());
		}
		existingChat.setLastUpdated(Timestamp.from(Instant.now()));

		if (updatedChat.getMessages() != null) {
			existingChat.getMessages().clear();
			updatedChat.getMessages().forEach(message -> {
				// If the message has an ID, it's an existing message that needs to be merged
				// If it doesn't have an ID, it's a new message
				if (message.getId() != null) {
					// Merge the detached message back into the session
					ChatMessage managedMessage = entityManager.merge(message);
					managedMessage.setChat(existingChat);
					existingChat.addMessage(managedMessage);
				} else {
					// New message - just set the chat reference
					message.setChat(existingChat);
					existingChat.addMessage(message);
				}
			});
		}

		chatDao.save(existingChat);
		return existingChat;
	}

	@Override
	@Transactional
	public void deleteChat(Run run, Workgroup workgroup, Long chatId) {
		Chat chat = chatDao.getChatById(chatId);
		if (chat == null) {
			throw new IllegalArgumentException("Chat not found with id: " + chatId);
		}

		// Verify the chat belongs to the specified run and workgroup
		if (!chat.getRun().equals(run) || !chat.getWorkgroup().equals(workgroup)) {
			throw new IllegalArgumentException("Chat does not belong to the specified run and workgroup");
		}
		chat.setDeleted(true);
		chatDao.save(chat);
	}
}

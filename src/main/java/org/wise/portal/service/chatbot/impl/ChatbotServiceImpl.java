package org.wise.portal.service.chatbot.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.dao.chatbot.ChatDao;
import org.wise.portal.dao.run.RunDao;
import org.wise.portal.dao.workgroup.WorkgroupDao;
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

	@Autowired
	private RunDao<Run> runDao;

	@Autowired
	private WorkgroupDao<Workgroup> workgroupDao;

	@Override
	@Transactional(readOnly = true)
	public List<Chat> getAllChats(Long runId, Long workgroupId) {
		try {
			Run run = runDao.getById(runId);
			Workgroup workgroup = workgroupDao.getById(workgroupId);
			return chatDao.getChatsByRunAndWorkgroup(run, workgroup);
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Run or Workgroup not found", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Chat getChat(Long runId, Long workgroupId, Long chatId) {
		Chat chat = chatDao.getChatById(chatId);
		if (chat == null) {
			throw new IllegalArgumentException("Chat not found with id: " + chatId);
		}
		// Verify the chat belongs to the specified run and workgroup
		if (!chat.getRun().getId().equals(runId) || !chat.getWorkgroup().getId().equals(workgroupId)) {
			throw new IllegalArgumentException("Chat does not belong to the specified run and workgroup");
		}
		return chat;
	}

	@Override
	@Transactional
	public Chat createChat(Long runId, Long workgroupId, Chat chat) {
		try {
			Run run = runDao.getById(runId);
			Workgroup workgroup = workgroupDao.getById(workgroupId);
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
		} catch (ObjectNotFoundException e) {
			throw new IllegalArgumentException("Run or Workgroup not found", e);
		}
	}

	@Override
	@Transactional
	public Chat updateChat(Long runId, Long workgroupId, Long chatId, Chat updatedChat) {
		Chat existingChat = chatDao.getChatById(chatId);
		if (existingChat == null) {
			throw new IllegalArgumentException("Chat not found with id: " + chatId);
		}

		// Verify the chat belongs to the specified run and workgroup
		if (!existingChat.getRun().getId().equals(runId)
		    || !existingChat.getWorkgroup().getId().equals(workgroupId)) {
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
	public void deleteChat(Long runId, Long workgroupId, Long chatId) {
		Chat chat = chatDao.getChatById(chatId);
		if (chat == null) {
			throw new IllegalArgumentException("Chat not found with id: " + chatId);
		}

		// Verify the chat belongs to the specified run and workgroup
		if (!chat.getRun().getId().equals(runId) || !chat.getWorkgroup().getId().equals(workgroupId)) {
			throw new IllegalArgumentException("Chat does not belong to the specified run and workgroup");
		}
		chat.setDeleted(true);
		chatDao.save(chat);
	}
}

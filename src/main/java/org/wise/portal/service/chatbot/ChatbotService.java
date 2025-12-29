package org.wise.portal.service.chatbot;

import java.util.List;

import org.wise.vle.domain.chatbot.Chat;

/**
 * Service interface for managing chatbot conversations
 * 
 * @author Hiroki Terashima
 */
public interface ChatbotService {

	/**
	 * Get all chats for a specific run and workgroup
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @return list of all chats
	 */
	List<Chat> getAllChats(Long runId, Long workgroupId);

	/**
	 * Get a specific chat by ID
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @return the requested chat
	 */
	Chat getChat(Long runId, Long workgroupId, Long chatId);

	/**
	 * Create a new chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chat the chat data
	 * @return the created chat with generated ID
	 */
	Chat createChat(Long runId, Long workgroupId, Chat chat);

	/**
	 * Update an existing chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @param chat the updated chat data
	 * @return the updated chat
	 */
	Chat updateChat(Long runId, Long workgroupId, Long chatId, Chat chat);

	/**
	 * Delete a chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 */
	void deleteChat(Long runId, Long workgroupId, Long chatId);
}

package org.wise.portal.service.chatbot;

import java.util.List;
import java.util.Map;

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
	List<Map<String, Object>> getAllChats(Long runId, Long workgroupId);

	/**
	 * Get a specific chat by ID
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @return the requested chat
	 */
	Map<String, Object> getChat(Long runId, Long workgroupId, String chatId);

	/**
	 * Create a new chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatData the chat data
	 * @return the created chat with generated ID
	 */
	Map<String, Object> createChat(Long runId, Long workgroupId, Map<String, Object> chatData);

	/**
	 * Update an existing chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @param chatData the updated chat data
	 * @return the updated chat
	 */
	Map<String, Object> updateChat(Long runId, Long workgroupId, String chatId,
	    Map<String, Object> chatData);

	/**
	 * Delete a chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @return success response
	 */
	Map<String, String> deleteChat(Long runId, Long workgroupId, String chatId);
}

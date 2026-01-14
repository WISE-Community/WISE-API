package org.wise.portal.service.chatbot;

import java.util.List;

import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
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
	 * @param run the run 
	 * @param workgroup the workgroup 
	 * @return list of all chats
	 */
	List<Chat> getAllChats(Run run, Workgroup workgroup);

	/**
	 * Create a new chat
	 * 
	 * @param run the run
	 * @param workgroup the workgroup
	 * @param chat the chat data
	 * @return the created chat
	 */
	Chat createChat(Run run, Workgroup workgroup, Chat chat);

	/**
	 * Update an existing chat
	 * 
	 * @param run the run
	 * @param workgroup the workgroup
	 * @param chatId the chat ID
	 * @param chat the updated chat data
	 * @return the updated chat
	 * @throws ObjectNotFoundException when the chat is not found 
	 */
	Chat updateChat(Run run, Workgroup workgroup, Long chatId, Chat chat)
	    throws ObjectNotFoundException;

	/**
	 * Delete a chat
	 * 
	 * @param run the run
	 * @param workgroup the workgroup
	 * @param chatId the chat ID
	 * @throws ObjectNotFoundException when the chat is not found 
	 */
	void deleteChat(Run run, Workgroup workgroup, Long chatId) throws ObjectNotFoundException;
}

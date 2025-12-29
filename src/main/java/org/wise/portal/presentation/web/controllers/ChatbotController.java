package org.wise.portal.presentation.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.chatbot.ChatbotService;
import org.wise.vle.domain.chatbot.Chat;

/**
 * REST controller for managing chatbot conversations
 * 
 * @author Hiroki Terashima
 */
@RestController
@RequestMapping("/api/chatbot")
@Secured("ROLE_USER")
public class ChatbotController {

	@Autowired
	private ChatbotService chatbotService;

	/**
	 * Get all chats for a specific run and workgroup
	 * 
	 * @param run the run ID
	 * @param workgroup the workgroup ID
	 * @return list of all chats
	 */
	@GetMapping("/chats/{run}/{workgroup}")
	public ResponseEntity<List<Chat>> getAllChats(@PathVariable RunImpl run,
	    @PathVariable WorkgroupImpl workgroup) {
		return ResponseEntity.ok(chatbotService.getAllChats(run, workgroup));
	}

	/**
	 * Create a new chat
	 * 
	 * @param run the run ID
	 * @param workgroup the workgroup ID
	 * @param chat the chat data
	 * @return the created chat 
	 */
	@PostMapping("/chats/{run}/{workgroup}")
	public ResponseEntity<Chat> createChat(@PathVariable RunImpl run,
	    @PathVariable WorkgroupImpl workgroup, @RequestBody Chat chat) {
		return ResponseEntity.status(HttpStatus.CREATED)
		    .body(chatbotService.createChat(run, workgroup, chat));
	}

	/**
	 * Update an existing chat
	 * 
	 * @param run the run ID
	 * @param workgroup the workgroup ID
	 * @param chatId the chat ID
	 * @param chat the updated chat data
	 * @return the updated chat
	 */
	@PutMapping("/chats/{run}/{workgroup}/{chatId}")
	public ResponseEntity<Chat> updateChat(@PathVariable RunImpl run,
	    @PathVariable WorkgroupImpl workgroup, @PathVariable Long chatId, @RequestBody Chat chat) {
		return ResponseEntity.ok(chatbotService.updateChat(run, workgroup, chatId, chat));
	}

	/**
	 * Delete a chat
	 * 
	 * @param run the run ID
	 * @param workgroup the workgroup ID
	 * @param chatId the chat ID
	 * @return success response
	 */
	@DeleteMapping("/chats/{run}/{workgroup}/{chatId}")
	public ResponseEntity<Void> deleteChat(@PathVariable RunImpl run,
	    @PathVariable WorkgroupImpl workgroup, @PathVariable Long chatId) {
		chatbotService.deleteChat(run, workgroup, chatId);
		return ResponseEntity.noContent().build();
	}
}

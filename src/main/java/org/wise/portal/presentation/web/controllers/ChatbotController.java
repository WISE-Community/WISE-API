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
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @return list of all chats
	 */
	@GetMapping("/chats/{runId}/{workgroupId}")
	public ResponseEntity<List<Chat>> getAllChats(@PathVariable Long runId,
	    @PathVariable Long workgroupId) {
		return ResponseEntity.ok(chatbotService.getAllChats(runId, workgroupId));
	}

	/**
	 * Get a specific chat by ID
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @return the requested chat
	 */
	@GetMapping("/chats/{runId}/{workgroupId}/{chatId}")
	public ResponseEntity<Chat> getChat(@PathVariable Long runId, @PathVariable Long workgroupId,
	    @PathVariable Long chatId) {
		return ResponseEntity.ok(chatbotService.getChat(runId, workgroupId, chatId));
	}

	/**
	 * Create a new chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chat the chat data
	 * @return the created chat with generated ID
	 */
	@PostMapping("/chats/{runId}/{workgroupId}")
	public ResponseEntity<Chat> createChat(@PathVariable Long runId, @PathVariable Long workgroupId,
	    @RequestBody Chat chat) {
		return ResponseEntity.status(HttpStatus.CREATED)
		    .body(chatbotService.createChat(runId, workgroupId, chat));
	}

	/**
	 * Update an existing chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @param chat the updated chat data
	 * @return the updated chat
	 */
	@PutMapping("/chats/{runId}/{workgroupId}/{chatId}")
	public ResponseEntity<Chat> updateChat(@PathVariable Long runId, @PathVariable Long workgroupId,
	    @PathVariable Long chatId, @RequestBody Chat chat) {
		return ResponseEntity.ok(chatbotService.updateChat(runId, workgroupId, chatId, chat));
	}

	/**
	 * Delete a chat
	 * 
	 * @param runId the run ID
	 * @param workgroupId the workgroup ID
	 * @param chatId the chat ID
	 * @return success response
	 */
	@DeleteMapping("/chats/{runId}/{workgroupId}/{chatId}")
	public ResponseEntity<Void> deleteChat(@PathVariable Long runId, @PathVariable Long workgroupId,
	    @PathVariable Long chatId) {
		chatbotService.deleteChat(runId, workgroupId, chatId);
		return ResponseEntity.noContent().build();
	}
}

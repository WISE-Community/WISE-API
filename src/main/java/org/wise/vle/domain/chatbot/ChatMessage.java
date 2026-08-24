/**
 * Copyright (c) 2007-2025 Regents of the University of California (Regents).
 * Created by WISE, Graduate School of Education, University of California, Berkeley.
 *
 * This software is distributed under the GNU General Public License, v3,
 * or (at your option) any later version.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.wise.vle.domain.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.wise.vle.domain.PersistableDomain;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Domain object representing a single message in a chatbot conversation
 *
 * @author Hiroki Terashima
 */
@Entity
@Table(name = "chatbot_messages", indexes = {
    @Index(columnList = "chatId", name = "chatbotMessagesChatIdIndex") })
@Getter
@Setter
public class ChatMessage extends PersistableDomain {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;

	@ManyToOne(targetEntity = Chat.class, cascade = { CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "chatId", nullable = false)
	@JsonIgnore
	private Chat chat;

	@Column(name = "role", length = 20, nullable = false)
	private String role; // "system", "user", or "assistant"

	@Column(name = "content", length = 65536, columnDefinition = "text", nullable = false)
	private String content;

	@Column(name = "timestamp", nullable = true)
	private Timestamp timestamp;

	@Column(name = "nodeId", length = 30, nullable = false)
	private String nodeId;

	@Transient
	private Long chatId;

	@Override
	protected Class<?> getObjectClass() {
		return ChatMessage.class;
	}

	public void convertToClientChatMessage() {
		if (this.getChat() != null) {
			this.setChatId(this.getChat().getId());
		}
	}
}

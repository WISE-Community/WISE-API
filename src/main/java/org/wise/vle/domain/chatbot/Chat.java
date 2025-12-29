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
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.vle.domain.PersistableDomain;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain object representing a chatbot conversation
 * 
 * @author Hiroki Terashima
 */
@Entity
@Table(name = "chatbot_chats", indexes = {
    @Index(columnList = "runId", name = "chatbotChatsRunIdIndex"),
    @Index(columnList = "workgroupId", name = "chatbotChatsWorkgroupIdIndex") })
@Getter
@Setter
public class Chat extends PersistableDomain {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id = null;

	@ManyToOne(targetEntity = RunImpl.class, cascade = {
	    CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "runId", nullable = false)
	@JsonIgnore
	private Run run;

	@ManyToOne(targetEntity = WorkgroupImpl.class, cascade = {
	    CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "workgroupId", nullable = false)
	@JsonIgnore
	private Workgroup workgroup;

	@Column(name = "title", length = 255, nullable = true)
	private String title;

	@Column(name = "createdAt", nullable = false)
	private Timestamp createdAt;

	@Column(name = "lastUpdated", nullable = false)
	private Timestamp lastUpdated;

	@Column(name = "isDeleted", nullable = false)
	private boolean isDeleted = false;

	@OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("timestamp ASC")
	private List<ChatMessage> messages = new ArrayList<>();

	@Transient
	private Long runId;

	@Transient
	private Long workgroupId;

	@Override
	protected Class<?> getObjectClass() {
		return Chat.class;
	}

	public void convertToClientChat() {
		this.setRunId(this.getRun().getId());
		this.setWorkgroupId(this.getWorkgroup().getId());
	}

	public void addMessage(ChatMessage message) {
		messages.add(message);
		message.setChat(this);
	}

	public void removeMessage(ChatMessage message) {
		messages.remove(message);
		message.setChat(null);
	}
}

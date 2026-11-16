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
package org.wise.portal.dao.chatbot.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.chatbot.ChatDao;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.chatbot.Chat;

/**
 * Hibernate implementation of ChatDao
 *
 * @author Hiroki Terashima
 */
@Repository
public class HibernateChatDao extends AbstractHibernateDao<Chat> implements ChatDao<Chat> {

	@Override
	protected Class<? extends Chat> getDataObjectClass() {
		return Chat.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Chat> getChatsByRunAndWorkgroup(Run run, Workgroup workgroup) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Chat> cq = cb.createQuery(Chat.class);
		Root<Chat> chatRoot = cq.from(Chat.class);
		List<Predicate> predicates = new ArrayList<>();

		if (run != null) {
			predicates.add(cb.equal(chatRoot.get("run"), run));
		}
		if (workgroup != null) {
			predicates.add(cb.equal(chatRoot.get("workgroup"), workgroup));
		}

		cq.select(chatRoot).where(predicates.toArray(new Predicate[predicates.size()]))
		    .orderBy(cb.desc(chatRoot.get("lastUpdated")));

		TypedQuery<Chat> query = entityManager.createQuery(cq);
		return (List<Chat>) (Object) query.getResultList();
	}
}

/**
 * Copyright (c) 2008-2015 Regents of the University of California (Regents).
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
package org.wise.portal.dao.newsitem.impl;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wise.portal.dao.impl.AbstractHibernateDao;
import org.wise.portal.dao.newsitem.NewsItemDao;
import org.wise.portal.domain.newsitem.NewsItem;
import org.wise.portal.domain.newsitem.NewsType;
import org.wise.portal.domain.newsitem.impl.NewsItemImpl;

/**
 * @author Patrick Lawler
 */
@Repository
public class HibernateNewsItemDao extends AbstractHibernateDao<NewsItem>
    implements NewsItemDao<NewsItem> {

  @Override
  protected Class<NewsItemImpl> getDataObjectClass() {
    return NewsItemImpl.class;
  }

  @Override
  public List<NewsItem> getNewsPageNews(NewsType type) {
    return getLatestNews(-1, type);
  }

  @Override
  public List<NewsItem> getHomePageNews(NewsType type) {
    return getLatestNews(3, type);
  }

  @Override
  public List<NewsItem> getAllNews() {
    return getLatestNews(-1, NewsType.PUBLIC_AND_TEACHER);
  }

  @SuppressWarnings("unchecked")
  private List<NewsItem> getLatestNews(int number, NewsType type) {
    CriteriaBuilder cb = getCriteriaBuilder();
    CriteriaQuery<NewsItemImpl> cq = cb.createQuery(NewsItemImpl.class);
    Root<NewsItemImpl> newsItemRoot = cq.from(NewsItemImpl.class);

    cq.select(newsItemRoot);
    if (type.equals(NewsType.PUBLIC_ONLY)) {
      cq.where(cb.equal(newsItemRoot.get("type"), "public"));
    }
    cq.orderBy(cb.desc(newsItemRoot.get("id")));

    TypedQuery<NewsItemImpl> query = entityManager.createQuery(cq);
    if (number >= 0) {
      query.setMaxResults(number);
    }
    return (List<NewsItem>) (Object) query.getResultList();
  }
}

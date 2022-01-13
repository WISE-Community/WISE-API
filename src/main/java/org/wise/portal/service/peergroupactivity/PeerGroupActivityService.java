/**
 * Copyright (c) 2008-2022 Regents of the University of California (Regents).
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
package org.wise.portal.service.peergroupactivity;

import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.run.Run;

/**
 * @author Hiroki Terashima
 */
public interface PeerGroupActivityService {

  /**
   * Retrieves PeerGroupActivity from the database or curriculum unit for the specified component
   *
   * @param run
   * @param nodeId
   * @param componentId
   * @return PeerGroupActivity
   * @throws PeerGroupActivityNotFoundException if PeerGroupActivity is not found in the
   *   database or curriculum unit for the specified component
   */
  PeerGroupActivity getByComponent(Run run, String nodeId, String componentId) throws
      PeerGroupActivityNotFoundException;

  /**
   * Retrieves PeerGroupActivity from the database for the specified run and tag. If none is found,
   * creates a new one.
   *
   * @param run
   * @param tag
   * @return PeerGroupActivity
   */
  PeerGroupActivity getByTag(Run run, String tag);
}

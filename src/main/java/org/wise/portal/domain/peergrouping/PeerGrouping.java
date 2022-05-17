/**
 * Copyright (c) 2008-2021 Regents of the University of California (Regents).
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
package org.wise.portal.domain.peergrouping;

import org.json.JSONException;
import org.wise.portal.domain.Persistable;
import org.wise.portal.domain.run.Run;

/**
 * A class that defines location of peer group activities and how to group workgroups together
 * @author Hiroki Terashima
 */
public interface PeerGrouping extends Persistable {

  Long getId();

  void setId(Long id);

  String getLogic();

  void setLogic(String logic);

  String getLogicComponentId() throws JSONException;

  String getLogicNodeId() throws JSONException;

  int getLogicThresholdCount();

  int getLogicThresholdPercent();

  int getMaxMembershipCount();

  void setMaxMembershipCount(int maxMembershipCount);

  Run getRun();

  void setRun(Run run);

  String getTag();

  void setTag(String tag);
}

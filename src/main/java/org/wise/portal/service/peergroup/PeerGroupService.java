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
package org.wise.portal.service.peergroup;

import java.util.List;

import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.peergroup.PeerGroup;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.workgroup.Workgroup;
import org.wise.vle.domain.work.StudentWork;

public interface PeerGroupService {

    /**
     * Gets the PeerGroup with the specified id
     * @param id Long PeerGroup's id
     * @return matched PeerGroup
     * @throws ObjectNotFoundException when PeerGroup with the given id is not found
     */
    public PeerGroup getById(Long id) throws ObjectNotFoundException;

    /**
     * Gets a PeerGroup for the specified workgroup and PeerGrouping if a PeerGroup
     * does not exist, create one.
     *
     * @param workgroup Workgroup to get/create the PeerGroup for
     * @param peerGrouping PeerGrouping to get/create the PeerGroup for
     * @return PeerGroup for the specified workgroup and PeerGrouping
     * @throws PeerGroupCreationException the PeerGroup cannot be created for other reasons
     * like an error occurred while grouping members
     */
    PeerGroup getPeerGroup(Workgroup workgroup, PeerGrouping peerGrouping)
            throws PeerGroupCreationException;

    /**
     * Gets all the PeerGroups for the specified PeerGrouping
     * @param peerGrouping PeerGrouping the PeerGroups works on
     * @return PeerGroups that work on the specified PeerGrouping
     */
    List<PeerGroup> getPeerGroups(PeerGrouping peerGrouping);

    /**
     * Gets StudentWork for the component from all the members in the PeerGroup
     * @param peerGroup group of workgroups in the PeerGroup
     * @return List of StudentWork by members in the PeerGroup for the component
     */
    public List<StudentWork> getStudentWork(PeerGroup peerGroup, String nodeId, String componentId);
}

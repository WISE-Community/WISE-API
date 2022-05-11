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
package org.wise.portal.service.peergrouping.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergrouping.PeerGroupingDao;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.project.impl.ProjectContent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.peergrouping.PeerGroupingNotFoundException;
import org.wise.portal.service.peergrouping.PeerGroupingService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupingServiceImpl implements PeerGroupingService {

  @Autowired
  private PeerGroupingDao<PeerGrouping> peerGroupingDao;

  @Autowired
  protected Environment appProperties;

  @Override
  public PeerGrouping getByComponent(Run run, String nodeId, String componentId)
      throws PeerGroupingNotFoundException {
    try {
      return getByTag(run, getPeerGroupingTag(run, nodeId, componentId));
    } catch (Exception e) {
      throw new PeerGroupingNotFoundException();
    }
  }

  @Override
  public PeerGrouping getByTag(Run run, String tag) {
    return peerGroupingDao.getByTag(run, tag);
  }

  private String getPeerGroupingTag(Run run, String nodeId, String componentId)
      throws JSONException, IOException {
    ProjectContent projectContent = getProjectContent(run);
    ProjectComponent component = projectContent.getComponent(nodeId, componentId);
    return component.getPeerGroupingTag();
  }

  private ProjectContent getProjectContent(Run run) throws IOException, JSONException {
    String projectFilePath = appProperties.getProperty("curriculum_base_dir") +
        run.getProject().getModulePath();
    String projectString = FileUtils.readFileToString(new File(projectFilePath));
    return new ProjectContent(new JSONObject(projectString));
  }

  public Set<PeerGrouping> createPeerGroupings(Run run) throws IOException, JSONException {
    Set<PeerGrouping> peerGroupings = getPeerGroupingsInUnit(run);
    peerGroupings.forEach(peerGrouping -> {
      peerGroupingDao.save(peerGrouping);
    });
    return peerGroupings;
  }

  private Set<PeerGrouping> getPeerGroupingsInUnit(Run run)
      throws IOException, JSONException {
    Set<PeerGrouping> peerGroupings = new HashSet<PeerGrouping>();
    ProjectContent projectContent = getProjectContent(run);
    JSONArray peerGroupingsInContent = projectContent.getPeerGroupings();
    if (peerGroupingsInContent != null) {
      for (int i = 0; i < peerGroupingsInContent.length(); i++) {
        JSONObject peerGroupingInContent = peerGroupingsInContent.optJSONObject(i);
        PeerGrouping peerGrouping = new PeerGroupingImpl(
            run,
            peerGroupingInContent.optString("tag"),
            peerGroupingInContent.optString("logic"),
            peerGroupingInContent.optInt("logicThresholdCount"),
            peerGroupingInContent.optInt("logicThresholdPercent"),
            peerGroupingInContent.optInt("maxMembershipCount")
        );
        peerGroupings.add(peerGrouping);
      }
    }
    return peerGroupings;
  }

  public PeerGrouping createPeerGrouping(Run run, PeerGrouping peerGrouping) {
    peerGrouping.setRun(run);
    peerGroupingDao.save(peerGrouping);
    return peerGrouping;
  }

  public PeerGrouping updatePeerGrouping(Run run, String tag, PeerGrouping peerGrouping) {
    PeerGrouping peerGroupingInDB = getByTag(run, tag);
    peerGroupingInDB.setLogic(peerGrouping.getLogic());
    peerGroupingInDB.setTag(peerGrouping.getTag());
    peerGroupingInDB.setMaxMembershipCount(peerGrouping.getMaxMembershipCount());
    peerGroupingDao.save(peerGroupingInDB);
    return peerGroupingInDB;
  }
}

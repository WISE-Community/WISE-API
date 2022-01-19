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
package org.wise.portal.service.peergroupactivity.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.wise.portal.dao.peergroupactivity.PeerGroupActivityDao;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.project.impl.ProjectComponent;
import org.wise.portal.domain.project.impl.ProjectContent;
import org.wise.portal.domain.run.Run;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityNotFoundException;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityService;

/**
 * @author Hiroki Terashima
 */
@Service
public class PeerGroupActivityServiceImpl implements PeerGroupActivityService {

  @Autowired
  private PeerGroupActivityDao<PeerGroupActivity> peerGroupActivityDao;

  @Autowired
  protected Environment appProperties;

  @Override
  public PeerGroupActivity getByComponent(Run run, String nodeId, String componentId)
      throws PeerGroupActivityNotFoundException {
    PeerGroupActivity activity = peerGroupActivityDao.getByComponent(run, nodeId, componentId);
    if (activity == null) {
      activity = getPeerGroupActivityFromUnit(run, nodeId, componentId);
      peerGroupActivityDao.save(activity);
    }
    return activity;
  }

  @Override
  public PeerGroupActivity getByTag(Run run, String tag) {
    PeerGroupActivity activity = peerGroupActivityDao.getByTag(run, tag);
    if (activity == null) {
      activity = new PeerGroupActivityImpl(run, tag);
      peerGroupActivityDao.save(activity);
    }
    return activity;
  }

  private PeerGroupActivity getPeerGroupActivityFromUnit(Run run, String nodeId,
      String componentId) throws PeerGroupActivityNotFoundException {
    try {
      ProjectContent projectContent = getProjectContent(run);
      ProjectComponent component = projectContent.getComponent(nodeId, componentId);
      if (component != null) {
        return new PeerGroupActivityImpl(run, nodeId, component);
      }
    } catch (Exception e) {
    }
    throw new PeerGroupActivityNotFoundException();
  }

  private ProjectContent getProjectContent(Run run) throws IOException, JSONException {
    String projectFilePath = appProperties.getProperty("curriculum_base_dir") +
        run.getProject().getModulePath();
    String projectString = FileUtils.readFileToString(new File(projectFilePath));
    return new ProjectContent(new JSONObject(projectString));
  }

  @Override
  public Set<PeerGroupActivity> getByRun(Run run) {
    Set<PeerGroupActivity> activities = new HashSet<PeerGroupActivity>();
    try {
      getPeerGroupActivityTagsInUnit(run).forEach(tag -> {
        activities.add(getByTag(run, tag));
      });
    } catch (IOException | JSONException e) {
    }
    return activities;
  }

  private Set<String> getPeerGroupActivityTagsInUnit(Run run) throws IOException, JSONException {
    ProjectContent projectContent = getProjectContent(run);
    return projectContent.getPeerGroupActivityTags();
  }
}

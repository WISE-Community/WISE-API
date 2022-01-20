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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.env.Environment;
import org.wise.portal.dao.peergroupactivity.PeerGroupActivityDao;
import org.wise.portal.domain.peergroupactivity.PeerGroupActivity;
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergroupactivity.impl.PeerGroupActivityServiceImpl;

/**
 * @author Hiroki Terashima
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(FileUtils.class)
public class PeerGroupActivityServiceImplTest {

  @TestSubject
  private PeerGroupActivityService service = new PeerGroupActivityServiceImpl();

  @Mock
  private PeerGroupActivityDao<PeerGroupActivity> peerGroupActivityDao;

  @Mock
  private Environment appProperties;

  private Run run = new RunImpl();

  private String nodeId = "node1";

  private String componentIdWithPGActivity = "component1";

  private String componentIdWithoutPGActivity = "component2";

  private String logic = "[{“name”: “maximizeSimilarIdeas”, “nodeId”: “node1”, “componentId”: “xyz”}]";

  private int logicThresholdCount = 10;

  private int logicThresholdPercent = 50;

  private int maxMembershipCount = 2;

  String tagInDB = "existingPeerGroupActivityTag";

  String tagNotInDB = "newPeerGroupActivityTag";

  PeerGroupActivity peerGroupActivity = new PeerGroupActivityImpl();

  private String projectJSONString = "{\"nodes\":[{\"id\":\"" + nodeId + "\"," +
      "\"components\":[" +
      "{\"id\":\"" + componentIdWithPGActivity + "\",\"logic\":\"" + logic + "\"," +
      "\"logicThresholdCount\":\"" + logicThresholdCount + "\"," +
      "\"logicThresholdPercent\":\"" + logicThresholdPercent + "\"," +
      "\"maxMembershipCount\":\"" + maxMembershipCount + "\"," +
      "\"peerGroupActivityTag\":\"" + tagInDB + "\"" +
      "}, {\"id\":\"" + componentIdWithoutPGActivity + "\"}]}]}";

  @Before
  public void setUp() {
    PowerMock.mockStatic(FileUtils.class);
    Project project = new ProjectImpl();
    project.setModulePath("/1/project.json");
    run.setProject(project);
  }

  @Test
  public void getByComponent_foundInDB_ReturnPeerGroupActivity() throws
      PeerGroupActivityNotFoundException {
    PeerGroupActivity peerGroupActivity = new PeerGroupActivityImpl();
    expect(peerGroupActivityDao.getByComponent(run, nodeId,
        componentIdWithPGActivity)).andReturn(peerGroupActivity);
    replay(peerGroupActivityDao);
    assertEquals(peerGroupActivity, service.getByComponent(run, nodeId,
        componentIdWithPGActivity));
    verify(peerGroupActivityDao);
  }

  @Test
  public void getByComponent_notInDBButInContent_ReturnPeerGroupActivity() throws
      IOException, PeerGroupActivityNotFoundException {
    expect(peerGroupActivityDao.getByComponent(run, nodeId,
        componentIdWithPGActivity)).andReturn(null);
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn("/var/curriculum");
    expect(FileUtils.readFileToString(isA(File.class))).andReturn(projectJSONString);
    peerGroupActivityDao.save(isA(PeerGroupActivity.class));
    expectLastCall();
    replayAll();
    PeerGroupActivity activity = service.getByComponent(run, nodeId,
        componentIdWithPGActivity);
    assertEquals(logic, activity.getLogic());
    assertEquals(logicThresholdCount, activity.getLogicThresholdCount());
    assertEquals(logicThresholdPercent, activity.getLogicThresholdPercent());
    assertEquals(maxMembershipCount, activity.getMaxMembershipCount());
    verifyAll();
  }

  @Test
  public void getByComponent_notInDBAndNotInContent_ThrowException() throws IOException {
    expect(peerGroupActivityDao.getByComponent(run, nodeId, componentIdWithoutPGActivity))
        .andReturn(null);
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn("/var/curriculum");
    expect(FileUtils.readFileToString(isA(File.class))).andReturn(projectJSONString);
    replayAll();
    try {
      service.getByComponent(run, nodeId, componentIdWithoutPGActivity);
      fail("PeerGroupActivityNotFoundException expected to be thrown, but was not thrown");
    } catch (PeerGroupActivityNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getByTag_notInDB_SaveAndReturnNewPeerGroupActivity() {
    expect(peerGroupActivityDao.getByTag(run, tagNotInDB)).andReturn(null);
    peerGroupActivityDao.save(isA(PeerGroupActivity.class));
    expectLastCall();
    replayAll();
    PeerGroupActivity activity = service.getByTag(run, tagNotInDB);
    assertEquals(tagNotInDB, activity.getTag());
    verifyAll();
  }

  @Test
  public void getByTag_foundInDB_ReturnPeerGroupActivity() {
    expect(peerGroupActivityDao.getByTag(run, tagInDB)).andReturn(peerGroupActivity);
    replayAll();
    assertEquals(peerGroupActivity, service.getByTag(run, tagInDB));
    verifyAll();
  }

  @Test
  public void getByRun_ReturnPeerGroupActivitiesInRunProject() throws IOException {
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn("/var/curriculum");
    expect(FileUtils.readFileToString(isA(File.class))).andReturn(projectJSONString);
    expect(peerGroupActivityDao.getByTag(run, tagInDB)).andReturn(peerGroupActivity);
    replayAll();
    assertEquals(1, service.getByRun(run).size());
    verifyAll();
  }
}

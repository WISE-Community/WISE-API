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
package org.wise.portal.service.peergrouping;

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
import org.wise.portal.dao.peergrouping.PeerGroupingDao;
import org.wise.portal.domain.peergrouping.PeerGrouping;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.domain.project.Project;
import org.wise.portal.domain.project.impl.ProjectImpl;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.run.impl.RunImpl;
import org.wise.portal.service.peergrouping.impl.PeerGroupingServiceImpl;

/**
 * @author Hiroki Terashima
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest(FileUtils.class)
public class PeerGroupingServiceImplTest {

  @TestSubject
  private PeerGroupingService service = new PeerGroupingServiceImpl();

  @Mock
  private PeerGroupingDao<PeerGrouping> peerGroupingDao;

  @Mock
  private Environment appProperties;

  private Run run = new RunImpl();

  private String nodeId = "node1";

  private String componentIdWithPeerGrouping = "component1";

  private String componentIdWithoutPeerGrouping = "component2";

  String tagInDB = "existingPeerGroupingTag";

  String tagNotInDB = "newPeerGroupingTag";

  PeerGrouping peerGrouping = new PeerGroupingImpl();

  private String projectJSONString = "{" +
      "\"peerGroupings\":[{\"tag\": \"" + tagInDB + "\"}]," +
      "\"nodes\":[{\"id\":\"" + nodeId + "\"," +
      "\"components\":[" +
      "{\"id\":\"" + componentIdWithPeerGrouping + "\"," +
      "\"peerGroupingTag\":\"" + tagInDB + "\"" +
      "}, {\"id\":\"" + componentIdWithoutPeerGrouping + "\"}]}]}";

  @Before
  public void setUp() {
    PowerMock.mockStatic(FileUtils.class);
    Project project = new ProjectImpl();
    project.setModulePath("/1/project.json");
    run.setProject(project);
    peerGrouping.setTag(tagInDB);
  }

  @Test
  public void getByComponent_TagNotInContent_ThrowException() throws IOException {
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn("/var/curriculum");
    expect(FileUtils.readFileToString(isA(File.class))).andReturn(projectJSONString);
    replayAll();
    try {
      service.getByComponent(run, nodeId, componentIdWithoutPeerGrouping);
      fail("PeerGroupingNotFoundException expected to be thrown, but was not thrown");
    } catch (PeerGroupingNotFoundException e) {
    }
    verifyAll();
  }

  @Test
  public void getByComponent_TagInContentAndInDB_ReturnPeerGroupingFromDB()
      throws IOException, PeerGroupingNotFoundException {
    expect(peerGroupingDao.getByTag(run, tagInDB)).andReturn(peerGrouping);
    expect(appProperties.getProperty("curriculum_base_dir")).andReturn("/var/curriculum");
    expect(FileUtils.readFileToString(isA(File.class))).andReturn(projectJSONString);
    replayAll();
    PeerGrouping peerGrouping = service.getByComponent(run, nodeId, componentIdWithPeerGrouping);
    assertEquals(tagInDB, peerGrouping.getTag());
    verifyAll();
  }

  @Test
  public void getByTag_foundInDB_ReturnPeerGrouping() {
    expect(peerGroupingDao.getByTag(run, tagInDB)).andReturn(peerGrouping);
    replayAll();
    assertEquals(peerGrouping, service.getByTag(run, tagInDB));
    verifyAll();
  }
}

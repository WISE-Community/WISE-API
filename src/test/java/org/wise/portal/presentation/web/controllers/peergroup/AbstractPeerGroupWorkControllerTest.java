package org.wise.portal.presentation.web.controllers.peergroup;

import static org.easymock.EasyMock.*;

import org.easymock.Mock;
import org.wise.portal.service.vle.wise5.StudentWorkService;

public abstract class AbstractPeerGroupWorkControllerTest
    extends AbstractPeerGroupAPIControllerTest {

  @Mock
  protected StudentWorkService studentWorkService;

  @Override
  protected void replayAll() {
    super.replayAll();
    replay(studentWorkService);
  }

  @Override
  protected void verifyAll() {
    super.verifyAll();
    verify(studentWorkService);
  }
}

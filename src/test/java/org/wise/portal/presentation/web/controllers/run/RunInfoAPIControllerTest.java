package org.wise.portal.presentation.web.controllers.run;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.ObjectNotFoundException;
import org.wise.portal.domain.run.Run;
import org.wise.portal.presentation.web.controllers.APIControllerTest;

@ExtendWith(EasyMockExtension.class)
public class RunInfoAPIControllerTest extends APIControllerTest {
  
  @TestSubject
  private RunInfoAPIController runInfoAPIController = new RunInfoAPIController();

  @BeforeEach
  public void setUp() {
    super.setUp();
  }
  
  @Test
  public void getRunInfoById_RunExistsInDB_ReturnRunInfo() throws ObjectNotFoundException {
    expect(userService.retrieveUserByUsername(TEACHER_USERNAME)).andReturn(teacher1);
    expect(userService.isUserAssociatedWithRun(teacher1, run1)).andReturn(true);
    replay(userService);
    expect(runService.retrieveById(runId1)).andReturn(run1);
    replay(runService);
    HashMap<String, Object> info = runInfoAPIController.getRunInfoById(teacherAuth, runId1);
    assertEquals("1", info.get("id"));
    assertEquals(RUN1_RUNCODE, info.get("runCode"));
    verify(runService);
  }

  @Test
  public void getRunInfoById_RunNotInDB_ReturnRunInfo() throws ObjectNotFoundException {
    Long runIdNotInDB = -1L;
    expect(runService.retrieveById(runIdNotInDB))
        .andThrow(new ObjectNotFoundException(runIdNotInDB, Run.class));
    replay(runService);
    HashMap<String, Object> info = runInfoAPIController.getRunInfoById(teacherAuth, runIdNotInDB);
    assertEquals(1, info.size());
    assertEquals("runNotFound", info.get("error"));
    verify(runService);
  }

  @Test
  public void getRunInfoByRunCode_RunExistsInDB_ReturnRunInfo() throws ObjectNotFoundException {
    expect(runService.retrieveRunByRuncode(RUN1_RUNCODE)).andReturn(run1);
    replay(runService);
    HashMap<String, Object> info = runInfoAPIController.getRunInfoByRunCode(RUN1_RUNCODE);
    assertEquals("1", info.get("id"));
    assertEquals(RUN1_RUNCODE, info.get("runCode"));
    verify(runService);
  }

  @Test
  public void getRunInfoByRunCode_RunNotInDB_ReturnRunInfo() throws ObjectNotFoundException {
    String runCodeNotInDB = "runCodeNotInDB";
    expect(runService.retrieveRunByRuncode(runCodeNotInDB))
        .andThrow(new ObjectNotFoundException(runCodeNotInDB, Run.class));
    replay(runService);
    HashMap<String, Object> info = runInfoAPIController.getRunInfoByRunCode(runCodeNotInDB);
    assertEquals(1, info.size());
    assertEquals("runNotFound", info.get("error"));
    verify(runService);
  }
}


package org.wise.portal.presentation.web.controllers;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.wise.portal.domain.group.Group;
import org.wise.portal.domain.run.Run;
import org.wise.portal.domain.user.User;

public class ControllerUtilTest {

  @Test
  public void getNumStudentsInRun_emptyPeriods_returnsZero() {
    Run run = mock(Run.class);
    expect(run.getPeriods()).andReturn(Collections.emptySet());
    replay(run);

    int count = ControllerUtil.getNumStudentsInRun(run);

    assertEquals(0, count);
    verify(run);
  }

  @Test
  public void getNumStudentsInRun_periodWithNoMembers_returnsZero() {
    Run run = mock(Run.class);
    Group period = mock(Group.class);
    expect(run.getPeriods()).andReturn(Collections.singleton(period));
    expect(period.getMembers()).andReturn(Collections.emptySet());
    replay(run, period);

    int count = ControllerUtil.getNumStudentsInRun(run);

    assertEquals(0, count);
    verify(run, period);
  }

  @Test
  public void getNumStudentsInRun_multiplePeriodsWithStudents_returnsTotalCount() {
    Run run = mock(Run.class);
    Group period1 = mock(Group.class);
    Group period2 = mock(Group.class);

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    User user3 = mock(User.class);

    Set<User> period1Students = new HashSet<>();
    period1Students.add(user1);
    period1Students.add(user2);

    Set<User> period2Students = new HashSet<>();
    period2Students.add(user3);

    Set<Group> periods = new HashSet<>();
    periods.add(period1);
    periods.add(period2);

    expect(run.getPeriods()).andReturn(periods);
    expect(period1.getMembers()).andReturn(period1Students);
    expect(period2.getMembers()).andReturn(period2Students);

    replay(run, period1, period2);

    int count = ControllerUtil.getNumStudentsInRun(run);

    assertEquals(3, count);
    verify(run, period1, period2);
  }
}

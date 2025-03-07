package org.wise.portal.domain.workgroup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wise.portal.domain.DomainTest;
import org.wise.portal.domain.workgroup.impl.WorkgroupImpl;
import org.wise.portal.service.workgroup.WorkgroupJsonModule;

public class WorkgroupTest extends DomainTest {

  Workgroup workgroup;

  @BeforeEach
  public void setup() {
    super.setup();
    workgroup = new WorkgroupImpl();
    workgroup.setId(123L);
    workgroup.setPeriod(period);
  }

  @Test
  public void serialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule((new WorkgroupJsonModule()));
    String json = mapper.writeValueAsString(workgroup);
    String expectedJson = "{\"id\":123,\"periodId\":100}";
    assertEquals(expectedJson, json);
  }
}

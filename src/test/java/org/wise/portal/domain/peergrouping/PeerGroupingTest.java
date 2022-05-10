package org.wise.portal.domain.peergrouping;


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wise.portal.domain.DomainTest;
import org.wise.portal.domain.peergrouping.impl.PeerGroupingImpl;
import org.wise.portal.service.peergrouping.PeerGroupingDeserializer;
import org.wise.portal.service.peergrouping.PeerGroupingJsonModule;
import org.wise.portal.service.peergrouping.PeerGroupingSerializer;
import org.wise.portal.service.run.RunService;

@RunWith(EasyMockRunner.class)
public class PeerGroupingTest extends DomainTest {

  PeerGrouping peerGrouping;

  ObjectMapper mapper;

  @Mock
  RunService runService;

  PeerGroupingJsonModule peerGroupingJsonModule = new PeerGroupingJsonModule();

  String peerGroupingJSON = "{\"id\":1,\"runId\":1,\"logic\":\"manual\",\"tag\":\"tag1\","
      + "\"maxMembershipCount\":2}";

  @Before
  public void setup() {
    super.setup();
    PeerGroupingDeserializer deserializer = new PeerGroupingDeserializer();
    deserializer.setRunService(runService);
    mapper = new ObjectMapper();
    peerGroupingJsonModule.addSerializer(PeerGroupingImpl.class,
        new PeerGroupingSerializer());
    peerGroupingJsonModule.addDeserializer(PeerGroupingImpl.class, deserializer);
    mapper.registerModule(peerGroupingJsonModule);
    peerGrouping = new PeerGroupingImpl();
    peerGrouping.setId(1L);
    peerGrouping.setRun(run);
    peerGrouping.setLogic("manual");
    peerGrouping.setTag("tag1");
    peerGrouping.setMaxMembershipCount(2);
  }

  @Test
  public void serialize() throws Exception {
    String json = mapper.writeValueAsString(peerGrouping);
    assertEquals(peerGroupingJSON, json);
  }

  @Test
  public void deserialize() throws Exception {
    expect(runService.retrieveById(1L)).andReturn(run);
    replay(runService);
    PeerGrouping peerGrouping =
        mapper.readValue(peerGroupingJSON, PeerGroupingImpl.class);
    assertEquals(1L, peerGrouping.getId().intValue());
    assertEquals("manual", peerGrouping.getLogic());
    assertEquals("tag1", peerGrouping.getTag());
    assertEquals(2, peerGrouping.getMaxMembershipCount());
    verify(runService);
  }
}

package org.wise.portal.domain.peergroupactivity;


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
import org.wise.portal.domain.peergroupactivity.impl.PeerGroupActivityImpl;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityDeserializer;
import org.wise.portal.service.peergroupactivity.PeerGroupActivityJsonModule;
import org.wise.portal.service.peergroupactivity.PeerGroupActivitySerializer;
import org.wise.portal.service.run.RunService;

@RunWith(EasyMockRunner.class)
public class PeerGroupActivityTest extends DomainTest {

  PeerGroupActivity activity;

  ObjectMapper mapper;

  @Mock
  RunService runService;

  PeerGroupActivityJsonModule peerGroupActivityJsonModule = new PeerGroupActivityJsonModule();

  String peerGroupActivityJSON = "{\"id\":1,\"runId\":1,\"logic\":\"manual\",\"tag\":\"tag1\","
      + "\"maxMembershipCount\":2}";

  @Before
  public void setup() {
    super.setup();
    PeerGroupActivityDeserializer deserializer = new PeerGroupActivityDeserializer();
    deserializer.setRunService(runService);
    mapper = new ObjectMapper();
    peerGroupActivityJsonModule.addSerializer(PeerGroupActivityImpl.class,
        new PeerGroupActivitySerializer());
    peerGroupActivityJsonModule.addDeserializer(PeerGroupActivityImpl.class, deserializer);
    mapper.registerModule(peerGroupActivityJsonModule);
    activity = new PeerGroupActivityImpl();
    activity.setId(1L);
    activity.setRun(run);
    activity.setLogic("manual");
    activity.setTag("tag1");
    activity.setMaxMembershipCount(2);
  }

  @Test
  public void serialize() throws Exception {
    String json = mapper.writeValueAsString(activity);
    assertEquals(peerGroupActivityJSON, json);
  }

  @Test
  public void deserialize() throws Exception {
    expect(runService.retrieveById(1L)).andReturn(run);
    replay(runService);
    PeerGroupActivity activity =
        mapper.readValue(peerGroupActivityJSON, PeerGroupActivityImpl.class);
    assertEquals(1L, activity.getId().intValue());
    assertEquals("manual", activity.getLogic());
    assertEquals("tag1", activity.getTag());
    assertEquals(2, activity.getMaxMembershipCount());
    verify(runService);
  }
}

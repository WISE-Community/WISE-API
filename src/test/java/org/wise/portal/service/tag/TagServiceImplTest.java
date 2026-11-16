package org.wise.portal.service.tag;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wise.portal.dao.project.TagDao;
import org.wise.portal.domain.Tag;
import org.wise.portal.domain.impl.TagImpl;
import org.wise.portal.service.tag.impl.TagServiceImpl;

@ExtendWith(EasyMockExtension.class)
public class TagServiceImplTest {

  @TestSubject
  private TagService tagServiceImpl = new TagServiceImpl();

  @Mock
  private TagDao<Tag> tagDao;

  private Tag tag;

  @BeforeEach
  public void setup() {
    tag = new TagImpl();
    tag.setId(1);
  }

  @Test
  public void updateTag_ExistingTag_ShouldUpdateTag() {
    tagDao.save(tag);
    expectLastCall();
    replay(tagDao);
    Tag updatedTag = tagServiceImpl.updateTag(tag);
    assertEquals(tag.getId(), updatedTag.getId());
    verify(tagDao);
  }

}

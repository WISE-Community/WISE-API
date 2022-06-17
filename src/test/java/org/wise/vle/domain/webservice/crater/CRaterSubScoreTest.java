package org.wise.vle.domain.webservice.crater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CRaterSubScoreTest extends CRaterResponseSubItemTest {

  String subScoreXmlWithoutMinMax = "<score id=\"science\" score=\"1\" realNumberScore=\"0.2919\" />";
  String subScoreXmlWithMinMax =
      "<score id=\"science\" score=\"1\" realNumberScore=\"0.2919\" score_range_min=\"1\" score_range_max=\"5\" />";

  @Test
  public void constructor_NoRangeMinMax_FieldsShouldBeNull() throws Exception {
    CRaterSubScore score = new CRaterSubScore(getNode(subScoreXmlWithoutMinMax));
    assertEquals(1, score.getScore());
    assertEquals(0.2919, score.getRealNumberScore(), 0.00001);
    assertNull(score.getScoreRangeMin());
    assertNull(score.getScoreRangeMax());
  }

  @Test
  public void constructor_WithRangeMinMax_FieldsShouldBeSet() throws Exception {
    CRaterSubScore score = new CRaterSubScore(getNode(subScoreXmlWithMinMax));
    assertEquals(1, score.getScore());
    assertEquals(0.2919, score.getRealNumberScore(), 0.00001);
    assertEquals(1, score.getScoreRangeMin().intValue());
    assertEquals(5, score.getScoreRangeMax().intValue());
  }
}

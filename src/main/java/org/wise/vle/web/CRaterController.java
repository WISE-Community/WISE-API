/**
 * Copyright (c) 2008-2021 Regents of the University of California (Regents).
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
package org.wise.vle.web;

import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wise.vle.domain.webservice.crater.CRaterHttpClient;
import org.wise.vle.domain.webservice.crater.CRaterScoringResponse;
import org.wise.vle.domain.webservice.crater.CRaterVerificationRequest;
import org.wise.vle.domain.webservice.crater.CRaterScoringRequest;
import org.wise.vle.domain.webservice.crater.CRaterVerificationResponse;

@RestController
@RequestMapping("/api/c-rater")
public class CRaterController {

  @GetMapping("/verify")
  boolean verifyItemId(CRaterVerificationRequest request) {
    CRaterVerificationResponse cRaterResponse = CRaterHttpClient.getVerificationResponse(request);
    return cRaterResponse.isVerified();
  }

  @PostMapping("/score")
  HashMap<String, Object> scoreItem(@RequestBody CRaterScoringRequest request) {
    CRaterScoringResponse cRaterResponse = CRaterHttpClient.getScoringResponse(request);
    HashMap<String, Object> response = new HashMap<String, Object>();
    if (cRaterResponse.isSingleScore()) {
      response.put("score", cRaterResponse.getScore());
    } else {
      response.put("scores", cRaterResponse.getScores());
    }
    response.put("ideas", cRaterResponse.getIdeas());
    response.put("cRaterResponse", cRaterResponse.getResponse());
    return response;
  }
}

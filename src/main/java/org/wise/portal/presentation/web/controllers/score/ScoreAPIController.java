package org.wise.portal.presentation.web.controllers.score;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for single-page SCORE app built with Angular
 * @author Anthony Perritano
 * @author Hiroki Terashima
 */
@Controller
@RequestMapping("/score-ta")
public class ScoreAPIController {


  @GetMapping
  protected String showTeachingAssistant() {
    return "forward:/score/teachingassistant/dist/index.html";
  }

  @GetMapping(value = {"/test", "/test/**"})
  protected String showTest() {
    return "hello";
  }
}

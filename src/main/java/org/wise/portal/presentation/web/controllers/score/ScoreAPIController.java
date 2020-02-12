package org.wise.portal.presentation.web.controllers.score;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for single-page SCORE app built with Angular
 *
 * @author Anthony Perritano
 * @author Hiroki Terashima
 */
@Controller
@RequestMapping(value = {"/score-app"})
public class ScoreAPIController {

  /**
   * Invokes the SCORE Teaching Assistant Tool based without a run
   */
  @GetMapping
  protected String showTeachingAssistant(HttpServletRequest request, HttpServletResponse response) {
    return "forward:/score/teachingassistant/dist/index.html";
  }

  /**
   * Invokes the SCORE Teaching Assistant Tool based on the specified run
   *
   * @param appType type of app
   * @param runId   ID of the run
   */
  @GetMapping(value = {"/manage", "/manage/**", "/manage/{appType}/{runId}"})
  protected String launchTeachingAssistantMonitor(@PathVariable String appType,
                                                  @PathVariable Long runId,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
    System.out.println("TA - App ID to: " + appType);
    System.out.println("TA - RUN ID to: " + runId);
    if (appType != null && runId != null) {
      return "forward:/score/teachingassistant/dist/index.html?runId=" + runId.toString();
    }
    return "redirect:/index.html";
  }


}

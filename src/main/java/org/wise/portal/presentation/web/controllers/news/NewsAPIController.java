package org.wise.portal.presentation.web.controllers.news;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.newsitem.NewsItem;
import org.wise.portal.service.newsitem.NewsItemService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/news", produces = "application/json;charset=UTF-8")
public class NewsAPIController {

  @Autowired
  private NewsItemService newsItemService;

  @GetMapping()
  protected String getNews(@RequestParam Optional<Integer> num, @RequestParam Optional<String> type) {
    List<NewsItem> newsItems = newsItemService.retrieveLatestNewsItems(num, type);
    return this.convertNewsToJSONString(newsItems);
  }

  private String convertNewsToJSONString(List<NewsItem> newsItems) {
    JSONArray newsItemsJSON = this.getNewsItemsJSON(newsItems);
    return newsItemsJSON.toString();
  }

  private JSONArray getNewsItemsJSON(List<NewsItem> newsItems) {
    JSONArray newsItemsJSON = new JSONArray();
    for (NewsItem newsItem: newsItems) {
      newsItemsJSON.put(getNewsItemJSON(newsItem));
    }
    return newsItemsJSON;
  }

  private JSONObject getNewsItemJSON(NewsItem newsItem) {
    JSONObject newsItemJSON = new JSONObject();
    try {
      newsItemJSON.put("id", newsItem.getId());
      newsItemJSON.put("date", newsItem.getDate());
      newsItemJSON.put("news", newsItem.getNews());
      newsItemJSON.put("title", newsItem.getTitle());
      newsItemJSON.put("type", newsItem.getType());
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return newsItemJSON;
  }
}

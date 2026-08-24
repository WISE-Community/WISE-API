package org.wise.portal.presentation.web.controllers.news;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wise.portal.domain.newsitem.NewsItem;
import org.wise.portal.domain.newsitem.NewsType;
import org.wise.portal.service.newsitem.NewsItemService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/news", produces = "application/json;charset=UTF-8")
public class NewsAPIController {

  @Autowired
  private NewsItemService newsItemService;

  @GetMapping()
  protected String getNewsPageNews(@RequestParam String type) {
    NewsType newsType = NewsType.stringToNewsType(type);
    List<NewsItem> newsItems = this.newsItemService.retrieveNewsPageNews(newsType);
    return this.getNewsItemsJSONString(newsItems);
  }

  @GetMapping("/home")
  protected String getHomePageNews(@RequestParam String type) {
    NewsType newsType = NewsType.stringToNewsType(type);
    List<NewsItem> newsItems = this.newsItemService.retrieveAndCacheHomePageNews(newsType);
    return this.getNewsItemsJSONString(newsItems);
  }

  private String getNewsItemsJSONString(List<NewsItem> newsItems) {
    JSONArray newsItemsJSON = new JSONArray();
    for (NewsItem newsItem: newsItems) {
      newsItemsJSON.put(getNewsItemJSON(newsItem));
    }
    return newsItemsJSON.toString();
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

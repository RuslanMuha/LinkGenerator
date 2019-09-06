package com.hitecher.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.hitecher.dto.URLDto;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.HttpStatusException;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;
import com.hitecher.exception.DoneParsingException;

@EnableBinding(Source.class)
public class LinksGeneratorService {
	static private Map<String, URLDto> hrefMap = new HashMap<>();

	@InboundChannelAdapter(Source.OUTPUT)
	public String sendSensorData() {
		return getLink();
	}

	private String getLink() {
		try {
		    for (int i = 0; ; i += 25) {
                parseUrl(i);
            }
        } catch (DoneParsingException e) {
            // do nothing, cos this means that LinkedIn returned not 2xx response
            System.err.println(e.getMessage());
        } catch (Exception e) {
		    e.printStackTrace();
        }

		return hrefMap + "";
	}

	private void parseUrl(int number) throws Exception {
        Connection.Response response;

	    try {
            String urlStart = "https://www.linkedin.com/jobs-guest/jobs/api/jobPostings/jobs?keywords=Developer&location=Israel&sortBy=DD&redirect=false&position=1&pageNum=0&start=";
            response = Jsoup.connect(urlStart + number).ignoreContentType(true)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .userAgent("User-Agent: MSIE 9.0; Windows NT 6.1; WOW64; Trident / 5.0")
                    .referrer("http://www.google.com")
                    .timeout(3000)
                    .followRedirects(false)
                    .execute();
        } catch (HttpStatusException e) {
	        String errMessage = "Finished batch requests to LinkedIn with statusCode = " + e.getStatusCode() + ". Fetched jobs = " + number;
	        throw new DoneParsingException(errMessage);
        } catch (SocketTimeoutException e){
	        throw new Exception(e.getMessage());
        }

        addLinksToSet(response);
	}

	private void addLinksToSet(Connection.Response response) throws IOException {
        Document doc = response.parse();

        Elements xElement = doc.getElementsByAttributeValueMatching("href", "/jobs/view");

        for (Element rez : xElement) {
            String href = rez.attr("href");
            // TODO save id for future
            String id = rez.parent().dataset().get("id");
            String shortLink = href.substring(0, href.indexOf("?refId"));
            URLDto urlDto = URLDto.builder().id(id).url(shortLink).build();
            System.out.println(urlDto.toString());
            hrefMap.put(id, urlDto);
        }
    }
}

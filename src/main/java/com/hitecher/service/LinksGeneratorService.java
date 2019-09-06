package com.hitecher.service;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.io.IOException;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.HttpStatusException;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;


@EnableBinding({Source.class})
public class LinksGeneratorService {
    static private List<String> hrefMap = new ArrayList<>();
    private int count = 0;
    private int k = 0;

    @InboundChannelAdapter(Source.OUTPUT)
    public String sendSensorData() {
        hrefMap.clear();
        count = 0;
        List<String> ids = null;
        try {
            ids = readFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getLink(ids);
    }

    @SneakyThrows
    private String getLink(List<String> listIds) {
        for (int i = 0; ; i += 25) {
            if (!parseUrl(i, listIds)) {
                break;
            }
        }
        Thread.sleep(120000);
        return hrefMap + "";
    }

    private boolean parseUrl(int number, List<String> listIds) throws Exception {
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
            return false;
        }

        return addLinksToSet(response, listIds);
    }

    private boolean addLinksToSet(Connection.Response response, List<String> listIds) throws IOException {
        Document doc = response.parse();

        Elements xElement = doc.getElementsByAttributeValueMatching("href", "/jobs/view");
        boolean fl = false;

        for (Element rez : xElement) {
            k++;
            String href = rez.attr("href");
            // TODO save id for future
            String shortLink = href.substring(0, href.indexOf("?refId"));


            if ((fl = !listIds.contains(shortLink)) && count <= 5) {
                if (count == 0) {
                    writeToFile(shortLink, false);
                } else writeToFile(shortLink, true);
                count++;
            }

            if (fl) {

                hrefMap.add(shortLink);
            } else break;

        }
        System.out.println("COUNTER : " + k);
        return fl;
    }

    private void writeToFile(String string, boolean notoverwrite) {
        try (FileWriter writer = new FileWriter("list_id.txt", notoverwrite)) {
            writer.write(string + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<String> readFromFile() throws IOException {
        List<String> listID = new ArrayList<>();
        Scanner in = new Scanner(new File("list_id.txt"));
        while (in.hasNext()) {
            if (!in.equals("\n")) {
                String id = in.nextLine();
                listID.add(id);
            }
        }

        return listID;
    }
}

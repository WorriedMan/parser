import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Parser {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("https://www.avito.ru/izhevsk/kvartiry/prodam").get();
        log(doc.title());
        Elements newsHeadlines = doc.select(".item");
        for (Element headline : newsHeadlines) {
            log(headline.attr("id"));
            headline.childNodes();
        }
    }

    private static void log(String title) {
        System.out.println(title);
    }
}

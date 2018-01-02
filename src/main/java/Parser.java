import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Parser {

    private static Boolean showDebug = false;

    public static void main(String[] args) throws FileNotFoundException {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(Calendar.getInstance().getTime());
        System.setOut(new PrintStream(new FileOutputStream("outputs/"+timeStamp+".txt")));
        Settings settings = new Settings();
        if (!settings.isLoaded()) {
            return;
        }
        MysqlConnection connection;
        try {
            connection = new MysqlConnection(settings.get("mysql_url"), settings.get("mysql_port"), settings.get("mysql_database"), settings.get("mysql_username"), settings.get("mysql_password"));
        } catch (IllegalStateException e) {
            writeData(e.getMessage());
            return;
        }

        Integer percentCount = Integer.valueOf(settings.get("percent_count"));
        Integer maxPages = Integer.valueOf(settings.get("pages"));
        Integer pageSleepTime = Integer.valueOf(settings.get("page_sleep_time"));
        Integer citySleepTime = Integer.valueOf(settings.get("city_sleep_time"));
        showDebug = Objects.equals(settings.get("debug"), "1");
        settings.getCitites().forEach(city -> loadCity(connection, city, maxPages, percentCount, citySleepTime, pageSleepTime));
        writeData("Completed");
    }

    private static void loadCity(MysqlConnection connection, String city, Integer maxPages, Integer percentCount, Integer sleepTime, Integer pageSleepTime) {
        writeData("Checking city: " + city);
        Integer[] result = {0, 0, 0, 0};
        try {
            for (int page = 1; page <= maxPages; page++) {
                Integer[] tempResult = loadAvitoPage(connection, String.valueOf(page), city);
                drawPercent(page, percentCount, maxPages);
                result = additionOfArrays(result, tempResult);
                TimeUnit.SECONDS.sleep(pageSleepTime);
            }
        } catch (IOException e) {
            if (e.getMessage().contains("Avito is blocked")) {
                writeData("Avito is blocked! Skipping this city...");
            } else if (!e.getMessage().contains("HTTP error fetching URL")) {
                writeData("Unable to load Avito: " + e.getMessage());
            } else {
                writeData("City is less than 100 pages");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            connection.addLog(result, city);
            try {
                writeData("Going to sleep...");
                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                writeData("Sleep is interrupted! " + e.getMessage());
            }
        }
    }

    private static Integer[] loadAvitoPage(MysqlConnection connection, String page, String city) throws IOException {
        Integer[] states = {0, 0, 0, 0};
        Document doc = Jsoup.connect("https://www.avito.ru/" + city + "/kvartiry/prodam?p=" + page).get();
//        Document doc = Jsoup.connect("http://localhost/file.html").get();
        Elements nulusElements = doc.select(".nulus");
        if (doc.location().contains("blocked")) {
            throw new IOException("Avito is blocked");
        }
        if (nulusElements.size() > 0) {
            throw new IOException("HTTP error fetching URL");
        }
        Elements newsHeadlines = doc.select(".item");
        writeLog("Total ads on page " + page + ": " + newsHeadlines.size());
        for (Element headline : newsHeadlines) {
            Apartment apartment = new Apartment(Integer.valueOf(headline.attr("id").substring(1)), headline);
            writeLog("Apt " + headline.attr("id") + " | rooms: " + apartment.getRooms() + " | meters: " + apartment.getMeters());
            if (apartment.getRooms() != null) {
                Integer result = connection.addApartment(apartment, city);
                writeLog("Write result: " + result);
                states[result]++;
            }
        }
        return states;
    }

    static void writeData(String text) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(Calendar.getInstance().getTime());
        System.out.println("[" + timeStamp + "] " + text);
    }

    static void writeLog(String text) {
        if (showDebug) {
            System.out.println(text);
        }
    }

    private static void drawPercent(Integer percent, Integer blockQuant, Integer maxPages) {
        Double oneBlock = Math.ceil(100 / maxPages);
        Integer percents = (int) (percent * oneBlock);
        StringBuilder percentString = new StringBuilder();
        Integer blocks = (int) (((double) blockQuant / 100) * percents);
        for (int i = 0; i < blocks; i++) {
            percentString.append("#");
        }
        for (int i = 0; i < blockQuant - blocks; i++) {
            percentString.append(".");
        }
        writeData("Checking Avito: " + percents + "% [" + percentString + "]");
    }

    private static Integer[] additionOfArrays(Integer[] array1, Integer[] array2) {
        Integer[] resultArray = new Integer[array1.length];
        for (int i = 0; i < array1.length; ++i) {
            resultArray[i] = array1[i] + array2[i];
        }
        return resultArray;
    }


}

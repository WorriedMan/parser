import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Apartment {
//    private static Pattern titlePattern = Pattern.compile("([1-9])-к",Pattern.UNICODE_CHARACTER_CLASS | Pattern.UNIX_LINES );
    private static Pattern titlePattern = Pattern.compile("([1-9])-к квартира, ([0-9.]+) .{2}, ([0-9]{1,2})/([0-9]{1,2})" );

    private Element parentNode;

    private Integer id;
    private Integer rooms;
    private Double meters;
    private Integer floor;
    private Integer maxFloors;
    private Double price;
    private String address;
    private String extendedDescription;
    private String developer;

    Apartment(Integer adId, Element parent) {
        id = adId;
        parentNode = parent;
        Description descriptionParent = getDescription();
        Parser.writeLog("descriptionParent: "+descriptionParent);
        if (descriptionParent != null && descriptionParent.element().child(0) != null) {
            Element title = descriptionParent.element().child(0).child(0);
            if (title != null && title.className().contains("item-description-title")) {
                Parser.writeLog("title text: "+title.text());
                Parser.writeLog("title pattern: "+titlePattern.toString());
                Matcher titleMatcher = titlePattern.matcher(title.text());
                if (titleMatcher.find()) {
                    rooms = Integer.valueOf(titleMatcher.group(1));
                    meters = Double.valueOf(titleMatcher.group(2));
                    floor = Integer.valueOf(titleMatcher.group(3));
                    maxFloors = Integer.valueOf(titleMatcher.group(4));
                }
            }
            if (descriptionParent.element().child(0).children().size() > 1) {
                Element priceElement = descriptionParent.element().child(0).child(1);
                if (priceElement != null) {
                    if (priceElement.text().contains("Договорная")) {
                        price = (double) -1;
                    } else if (priceElement.text().contains("Цена не указана")) {
                        price = (double) -1;
                    } else {
                        price = Double.valueOf(priceElement.text().replaceAll("[^0-9]", ""));
                    }
                }
                extendedDescription = descriptionParent.getExtendedDescription();
                address = descriptionParent.getAddress();
                developer = descriptionParent.getDeveloper();
                if (Objects.equals(developer, "")) {
                    developer = "Частное лицо";
                }
//                System.out.println(developer);
            }
        }
    }

    private Description getDescription() {
        Element description = parentNode.children().stream()
                .filter(element -> element.className().contains("description")).findFirst()
                .orElse(null);
        if (description == null) {
            return null;
        }
        return new Description(description);
    }

    Integer getRooms() {
        return rooms;
    }

    Double getMeters() {
        return meters;
    }

    Integer getFloor() {
        return floor;
    }

    Integer getMaxFloors() {
        return maxFloors;
    }

    Double getPrice() {
        return price;
    }

    String getAddress() {
        return address;
    }

    String getExtendedDescription() {
        return extendedDescription;
    }

    String getDeveloper() {
        return developer;
    }

    Integer getId() {
        return id;
    }
}

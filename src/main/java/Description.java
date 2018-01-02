import org.jsoup.nodes.Element;

class Description {

    private Element element;

    Description(Element baseElement) {
        element = baseElement;
    }

    String getExtendedDescription() {
        Element extendedDescriptionElement = element.children().stream()
                .filter(element -> element.className().contains("item_table-extended-description")).findFirst()
                .orElse(null);
        if (extendedDescriptionElement != null) {
            return extendedDescriptionElement.text();
        }
        return "";
    }

    String getAddress() {
        Element addressElement = element.children().stream()
                .filter(element -> element.children().size() > 0 && element.child(0).className().contains("address")).findFirst()
                .orElse(null);
        if (addressElement != null) {
            return addressElement.text();
        }
        return "";
    }

    String getDeveloper() {
        Element developerElement = element.children().stream()
                .filter(element -> element.className().contains("data")).findFirst()
                .orElse(null);
        if (developerElement != null) {
            return developerElement.text().replaceAll("Сегодня.+|Вчера.+|[0-9]{1,2} (января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря).+", "");
        }
        return "";
    }

    Element element() {
        return element;
    }
}

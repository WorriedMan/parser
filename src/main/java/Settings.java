import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class Settings {

    private static Pattern settingParent = Pattern.compile("(.+):(.+)");
    private boolean mLoaded = false;
    private HashMap<String, String> mSettings = new HashMap<>();
    private ArrayList<String> mCitites = new ArrayList<>();

    Settings() {
        try (Stream<String> stream = Files.lines(Paths.get("properties"))) {
            stream.forEach(this::addSetting);
            mLoaded = true;

        } catch (IOException e) {
            System.out.println("Unable to load settings file");
        }
    }

    private void addSetting(String data) {
        if (data.charAt(0) == '#') {
            return;
        }
        Matcher settingMatcher = settingParent.matcher(data);
        if (settingMatcher.find()) {
            String key = settingMatcher.group(1);
            String value = settingMatcher.group(2);
            if (Objects.equals(key, "city")) {
                loadCities(value);
            } else {
                mSettings.put(key, value);
            }
        }
    }

    private void loadCities(String value) {
        Pattern cityPattern = Pattern.compile("[a-z]+");
        Matcher citiesMatcher = cityPattern.matcher(value);
        while (citiesMatcher.find()) {
            mCitites.add(citiesMatcher.group());
        }
    }

    boolean isLoaded() {
        return mLoaded;
    }

    String get(String key) {
        if (mSettings.containsKey(key)) {
            return mSettings.get(key);
        }
        return null;
    }

    ArrayList<String> getCitites() {
        return mCitites;
    }
}

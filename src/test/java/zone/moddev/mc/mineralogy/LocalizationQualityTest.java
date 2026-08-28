package zone.moddev.mc.mineralogy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/** Quality policy for the reviewed 1.17.1 JSON localization inventory. */
public class LocalizationQualityTest {
    private static final File LANG_DIR = new File("src/main/resources/assets/mineralogy/lang");
    private static final Set<String> ENGLISH_LOCALES = new HashSet<String>(Arrays.asList(
            "en_ca.json", "en_en.json", "en_gb.json", "en_pt.json", "en_us.json"));
    private static final Set<String> NON_LATIN_LOCALES = new HashSet<String>(Arrays.asList(
            "ja_jp.json", "ko_kr.json", "ru_ru.json", "zh_cn.json"));
    private static final Pattern FORMAT_ARGUMENT = Pattern.compile("%(?:\\d+\\$)?[a-zA-Z%]");
    private static final Pattern LATIN_WORD = Pattern.compile("[A-HJ-Za-hj-z]{2,}");

    @Test
    public void everyLocaleIsUtf8JsonWithStableFormatting() throws Exception {
        Map<String, String> english = read(new File(LANG_DIR, "en_us.json"));
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);
        assertEquals(17, files.length);

        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            assertFalse(file.getName() + " must not contain a UTF-8 BOM",
                    bytes.length >= 3 && bytes[0] == (byte) 0xef
                            && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf);
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertFalse(file.getName() + " contains a replacement character", content.contains("\ufffd"));
            assertTrue(file.getName() + " must end with a newline", content.endsWith("\n"));
            for (String line : content.split("\\n", -1)) {
                assertEquals(file.getName() + " has trailing whitespace", line, line.replaceFirst("[ \\t]+$", ""));
            }

            Map<String, String> translations = read(file);
            assertEquals(file.getName(), new ArrayList<String>(english.keySet()),
                    new ArrayList<String>(translations.keySet()));
            for (String key : english.keySet()) {
                assertFalse(file.getName() + " has a blank value for " + key, translations.get(key).trim().isEmpty());
                assertEquals(file.getName() + " changes formatting arguments for " + key,
                        formatArguments(english.get(key)), formatArguments(translations.get(key)));
            }
        }
    }

    @Test
    public void nonEnglishLocalesOnlyMatchReviewedTechnicalTerms() throws Exception {
        Map<String, String> english = read(new File(LANG_DIR, "en_us.json"));
        Map<String, Set<String>> allowed = reviewedEnglishMatches();
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        for (File file : files) {
            if (ENGLISH_LOCALES.contains(file.getName())) continue;
            Map<String, String> translations = read(file);
            Set<String> actual = new HashSet<String>();
            for (String key : english.keySet()) {
                if (english.get(key).equals(translations.get(key))) actual.add(key);
            }
            assertEquals(file.getName() + " has an unreviewed English fallback",
                    allowed.containsKey(file.getName()) ? allowed.get(file.getName()) : Collections.emptySet(), actual);
        }
    }

    @Test
    public void nonEnglishLocalesContainNoEmbeddedEnglishUiFragments() throws Exception {
        Map<String, Pattern> forbidden = forbiddenEnglishByLocale();
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);
        for (File file : files) {
            if (ENGLISH_LOCALES.contains(file.getName())) continue;
            for (Map.Entry<String, String> entry : read(file).entrySet()) {
                if (NON_LATIN_LOCALES.contains(file.getName())) {
                    assertFalse(file.getName() + " contains an English word in " + entry,
                            LATIN_WORD.matcher(entry.getValue()).find());
                } else {
                    Pattern pattern = forbidden.get(file.getName());
                    assertNotNull("Missing forbidden-word policy for " + file.getName(), pattern);
                    assertFalse(file.getName() + " contains an English UI fragment in " + entry,
                            pattern.matcher(entry.getValue()).find());
                }
            }
        }
    }

    @Test
    public void representativeTranslationsCoverGeologyTabsAndOil() throws Exception {
        assertLocale("de_de.json", "block.mineralogy.shale", "Tonschiefer");
        assertLocale("de_de.json", "item.mineralogy.crude_oil_bucket", "Roh\u00f6leimer");
        assertLocale("es_es.json", "block.mineralogy.siltstone", "Limolita");
        assertLocale("fr_fr.json", "block.mineralogy.hornfels", "Corn\u00e9enne");
        assertLocale("pt_br.json", "block.mineralogy.shale", "Folhelho");
        assertLocale("ja_jp.json", "block.mineralogy.conglomerate", "\u792b\u5ca9");
        assertLocale("ko_kr.json", "block.mineralogy.gabbro", "\ubc18\ub824\uc554");
        assertLocale("ru_ru.json", "item.mineralogy.crude_oil_bucket", "\u0412\u0435\u0434\u0440\u043e \u0441\u044b\u0440\u043e\u0439 \u043d\u0435\u0444\u0442\u0438");
        assertLocale("zh_cn.json", "itemGroup.mineralogy.item", "\u77ff\u7269\u5b66\u7269\u54c1");
    }

    private static void assertLocale(String locale, String key, String expected) throws Exception {
        assertEquals(expected, read(new File(LANG_DIR, locale)).get(key));
    }

    private static Map<String, Set<String>> reviewedEnglishMatches() {
        Set<String> german = new HashSet<String>(Arrays.asList(
                "block.mineralogy.basalt", "block.mineralogy.gabbro", "block.mineralogy.hornfels"));
        Set<String> french = new HashSet<String>(Arrays.asList(
                "block.mineralogy.amphibolite", "block.mineralogy.chert", "block.mineralogy.diabase",
                "block.mineralogy.diorite", "block.mineralogy.gabbro", "block.mineralogy.gneiss",
                "block.mineralogy.granite", "block.mineralogy.nitrate_block",
                "block.mineralogy.novaculite", "block.mineralogy.pegmatite",
                "block.mineralogy.quartzite", "block.mineralogy.rhyolite"));
        Map<String, Set<String>> allowed = new HashMap<String, Set<String>>();
        allowed.put("de_au.json", german);
        allowed.put("de_de.json", german);
        allowed.put("fr_ca.json", french);
        allowed.put("fr_fr.json", french);
        return allowed;
    }

    private static Map<String, Pattern> forbiddenEnglishByLocale() {
        String common = "Polished|Wall|Furnace|Hoe|Pickaxe|Sword|Blank|Left|Right|Street|"
                + "Dust|Ore|Bucket|Fertilizer|Crude|Oil|Stairs|Slab|Brick|Smooth|Drywall|Axe|Cross";
        Map<String, Pattern> forbidden = new HashMap<String, Pattern>();
        forbidden.put("de_au.json", words(common));
        forbidden.put("de_de.json", words(common));
        forbidden.put("es_es.json", words(common + "|Hammer|Relief|Gabbro|Siltstone|Phyllite|Hornfels|Tuff|Slate|Scoria|Plus"));
        forbidden.put("es_mx.json", forbidden.get("es_es.json"));
        forbidden.put("fr_ca.json", words(common + "|Hammer|Siltstone|Phyllite|Tuff|Slate|Scoria"));
        forbidden.put("fr_fr.json", forbidden.get("fr_ca.json"));
        forbidden.put("pt_br.json", words(common + "|Hammer|Relief|Gabbro|Siltstone|Phyllite|Hornfels|Tuff|Slate|Scoria|Plus"));
        forbidden.put("pt_pt.json", forbidden.get("pt_br.json"));
        return forbidden;
    }

    private static Pattern words(String alternatives) {
        return Pattern.compile("\\b(?:" + alternatives + ")\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private static List<String> formatArguments(String value) {
        List<String> result = new ArrayList<String>();
        Matcher matcher = FORMAT_ARGUMENT.matcher(value);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }

    private static Map<String, String> read(File file) throws Exception {
        JsonObject object;
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            object = new JsonParser().parse(reader).getAsJsonObject();
        }
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            assertTrue(file.getName() + " contains a non-string value for " + entry.getKey(),
                    entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
            values.put(entry.getKey(), entry.getValue().getAsString());
        }
        return values;
    }
}

package zone.moddev.mc.mineralogy;

import org.junit.Test;

import java.io.File;
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

public class LocalizationQualityTest {
    private static final File LANG_DIR = new File("src/main/resources/assets/mineralogy/lang");
    private static final Set<String> ENGLISH_LOCALES = new HashSet<String>(Arrays.asList(
            "en_CA.lang", "en_EN.lang", "en_GB.lang", "en_PT.lang", "en_US.lang"));
    private static final Set<String> NON_LATIN_LOCALES = new HashSet<String>(Arrays.asList(
            "ja_JP.lang", "ko_KR.lang", "ru_RU.lang", "zh_CN.lang"));
    private static final Pattern FORMAT_ARGUMENT = Pattern.compile("%(?:\\d+\\$)?[a-zA-Z%]");
    private static final Pattern LATIN_WORD = Pattern.compile("[A-HJ-Za-hj-z]{2,}");

    @Test
    public void everyLocaleIsWellFormedUtf8WithStableFormatting() throws Exception {
        Map<String, String> english = read(new File(LANG_DIR, "en_US.lang"));
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);

        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            assertFalse(file.getName() + " must not contain a UTF-8 BOM",
                    bytes.length >= 3 && bytes[0] == (byte) 0xef
                            && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf);
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertFalse(file.getName() + " contains an invalid UTF-8 replacement character",
                    content.indexOf('\ufffd') >= 0);
            assertTrue(file.getName() + " must end with a newline", content.endsWith("\n"));

            for (String line : content.split("\\n", -1)) {
                assertEquals(file.getName() + " has trailing whitespace: " + line,
                        line, line.replaceFirst("[ \\t]+$", ""));
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                assertTrue(file.getName() + " has a malformed line: " + line, line.contains("="));
                int separator = line.indexOf('=');
                assertFalse(file.getName() + " has a blank key", line.substring(0, separator).trim().isEmpty());
                assertFalse(file.getName() + " has a blank value for " + line.substring(0, separator),
                        line.substring(separator + 1).trim().isEmpty());
            }

            Map<String, String> translations = read(file);
            for (String key : english.keySet()) {
                assertEquals(file.getName() + " changes formatting arguments for " + key,
                        formatArguments(english.get(key)), formatArguments(translations.get(key)));
            }
        }
    }

    @Test
    public void nonEnglishLocalesOnlyMatchEnglishForReviewedTechnicalTerms() throws Exception {
        Map<String, String> english = read(new File(LANG_DIR, "en_US.lang"));
        Map<String, Set<String>> allowed = reviewedEnglishMatches();
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);

        for (File file : files) {
            if (ENGLISH_LOCALES.contains(file.getName())) {
                continue;
            }
            Map<String, String> translations = read(file);
            Set<String> actualMatches = new HashSet<String>();
            for (String key : english.keySet()) {
                if (english.get(key).equals(translations.get(key))) {
                    actualMatches.add(key);
                }
            }
            assertEquals(file.getName() + " has an unreviewed English fallback",
                    allowed.containsKey(file.getName()) ? allowed.get(file.getName()) : Collections.emptySet(),
                    actualMatches);
        }
    }

    @Test
    public void nonEnglishLocalesContainNoEmbeddedEnglishUiFragments() throws Exception {
        Map<String, Pattern> forbidden = forbiddenEnglishByLocale();
        File[] files = LANG_DIR.listFiles((dir, name) -> name.endsWith(".lang"));
        assertNotNull(files);

        for (File file : files) {
            if (ENGLISH_LOCALES.contains(file.getName())) {
                continue;
            }
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
    public void representativeTranslationsUseReviewedGeologyAndMineralogySixTerms() throws Exception {
        Map<String, String> german = read(new File(LANG_DIR, "de_DE.lang"));
        assertEquals("Tonschiefer", german.get("tile.mineralogy.shale.name"));
        assertEquals("Polierter Ziegelofen aus Andesit",
                german.get("tile.mineralogy.andesite_smooth_brick_furnace.name"));
        assertEquals("Rohöleimer", german.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("Mineralogie-Stufen", german.get("itemGroup.mineralogy.slab"));

        Map<String, String> spanish = read(new File(LANG_DIR, "es_ES.lang"));
        assertEquals("Limolita", spanish.get("tile.mineralogy.siltstone.name"));
        assertEquals("Cubo de petróleo crudo", spanish.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("Rocas de Mineralogía", spanish.get("itemGroup.mineralogy.rock"));

        Map<String, String> french = read(new File(LANG_DIR, "fr_FR.lang"));
        assertEquals("Cornéenne", french.get("tile.mineralogy.hornfels.name"));
        assertEquals("Phosphore", french.get("tile.mineralogy.phosphorous_block.name"));
        assertEquals("Seau de pétrole brut", french.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("Murets de Minéralogie", french.get("itemGroup.mineralogy.wall"));

        Map<String, String> portuguese = read(new File(LANG_DIR, "pt_BR.lang"));
        assertEquals("Folhelho", portuguese.get("tile.mineralogy.shale.name"));
        assertEquals("Placa de gesso preta", portuguese.get("tile.mineralogy.drywall_black.name"));
        assertEquals("Balde de petróleo bruto", portuguese.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("Lajes da Mineralogia", portuguese.get("itemGroup.mineralogy.slab"));

        Map<String, String> japanese = read(new File(LANG_DIR, "ja_JP.lang"));
        assertEquals("礫岩", japanese.get("tile.mineralogy.conglomerate.name"));
        assertEquals("角閃岩", japanese.get("tile.mineralogy.amphibolite.name"));
        assertEquals("原油入りバケツ", japanese.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("鉱物学のアイテム", japanese.get("itemGroup.mineralogy.item"));

        Map<String, String> korean = read(new File(LANG_DIR, "ko_KR.lang"));
        assertEquals("역암", korean.get("tile.mineralogy.conglomerate.name"));
        assertEquals("반려암", korean.get("tile.mineralogy.gabbro.name"));
        assertEquals("원유 양동이", korean.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("광물학 반 블록", korean.get("itemGroup.mineralogy.slab"));

        Map<String, String> russian = read(new File(LANG_DIR, "ru_RU.lang"));
        assertEquals("Кремень", russian.get("tile.mineralogy.chert.name"));
        assertEquals("Барельеф: Аспидный сланец (знак плюса)",
                russian.get("tile.mineralogy.slate_relief_plus.name"));
        assertEquals("Ведро сырой нефти", russian.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("Минералогические породы", russian.get("itemGroup.mineralogy.rock"));

        Map<String, String> chinese = read(new File(LANG_DIR, "zh_CN.lang"));
        assertEquals("角闪岩", chinese.get("tile.mineralogy.amphibolite.name"));
        assertEquals("磨制凝灰岩", chinese.get("tile.mineralogy.tuff_smooth.name"));
        assertEquals("矿物学", chinese.get("itemGroup.mineralogyTab"));
        assertEquals("原油桶", chinese.get("item.mineralogy.crude_oil_bucket.name"));
        assertEquals("矿物学物品", chinese.get("itemGroup.mineralogy.item"));
    }

    private static Map<String, Set<String>> reviewedEnglishMatches() {
        Set<String> german = new HashSet<String>(Arrays.asList(
                "tile.mineralogy.basalt.name",
                "tile.mineralogy.gabbro.name",
                "tile.mineralogy.hornfels.name"));
        Set<String> french = new HashSet<String>(Arrays.asList(
                "tile.mineralogy.amphibolite.name",
                "tile.mineralogy.chert.name",
                "tile.mineralogy.diabase.name",
                "tile.mineralogy.diorite.name",
                "tile.mineralogy.gabbro.name",
                "tile.mineralogy.gneiss.name",
                "tile.mineralogy.granite.name",
                "tile.mineralogy.nitrate_block.name",
                "tile.mineralogy.novaculite.name",
                "tile.mineralogy.pegmatite.name",
                "tile.mineralogy.quartzite.name",
                "tile.mineralogy.rhyolite.name"));
        Map<String, Set<String>> allowed = new HashMap<String, Set<String>>();
        allowed.put("de_AU.lang", german);
        allowed.put("de_DE.lang", german);
        allowed.put("fr_CA.lang", french);
        allowed.put("fr_FR.lang", french);
        return allowed;
    }

    private static Map<String, Pattern> forbiddenEnglishByLocale() {
        String common = "Polished|Wall|Furnace|Hoe|Pickaxe|Sword|Blank|Left|Right|Street|"
                + "Dust|Ore|Bucket|Fertilizer|Crude|Oil|Stairs|Slab|Brick|Smooth|Drywall|Axe|Cross";
        Map<String, Pattern> forbidden = new HashMap<String, Pattern>();
        forbidden.put("de_AU.lang", words(common));
        forbidden.put("de_DE.lang", words(common));
        forbidden.put("es_ES.lang", words(common + "|Hammer|Relief|Gabbro|Siltstone|Phyllite|Hornfels|Tuff|Slate|Scoria|Plus"));
        forbidden.put("es_MX.lang", forbidden.get("es_ES.lang"));
        forbidden.put("fr_CA.lang", words(common + "|Hammer|Siltstone|Phyllite|Tuff|Slate|Scoria"));
        forbidden.put("fr_FR.lang", forbidden.get("fr_CA.lang"));
        forbidden.put("pt_BR.lang", words(common + "|Hammer|Relief|Gabbro|Siltstone|Phyllite|Hornfels|Tuff|Slate|Scoria|Plus"));
        forbidden.put("pt_PT.lang", forbidden.get("pt_BR.lang"));
        return forbidden;
    }

    private static Pattern words(String alternatives) {
        return Pattern.compile("\\b(?:" + alternatives + ")\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private static List<String> formatArguments(String value) {
        List<String> arguments = new ArrayList<String>();
        Matcher matcher = FORMAT_ARGUMENT.matcher(value);
        while (matcher.find()) {
            arguments.add(matcher.group());
        }
        return arguments;
    }

    private static Map<String, String> read(File file) throws Exception {
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf('=');
            assertTrue(file.getName() + " has a malformed line: " + line, separator > 0);
            String key = line.substring(0, separator);
            assertNull(file.getName() + " has duplicate key " + key,
                    values.put(key, line.substring(separator + 1)));
        }
        return values;
    }
}

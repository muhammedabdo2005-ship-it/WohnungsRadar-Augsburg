package de.wohnungsradar.augsburg;

import java.net.URI;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small platform-independent helpers; can be tested without Android. */
public final class ListingUtils {
    private ListingUtils() {}
    public static final String SCOUT = "de.is24.android";
    public static final String WELT = "de.immowelt.android.immobiliensuche";
    public static final String KLEIN = "com.ebay.kleinanzeigen";
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROOMS = Pattern.compile("(?i)(?<!\\d)(\\d+(?:[.,]\\d+)?)\\s*[- ]?zimmer(?:wohnung|haus|\\b)");

    public static String firstUrl(String text) {
        Matcher m = URL.matcher(text == null ? "" : text);
        if (!m.find()) return "";
        return m.group().replaceAll("[),.;]+$", "");
    }
    public static String sourceFor(String pkg, String text) {
        if (SCOUT.equals(pkg)) return "ImmoScout24";
        if (WELT.equals(pkg)) return "immowelt";
        if (KLEIN.equals(pkg)) return "Kleinanzeigen";
        String s = (text == null ? "" : text).toLowerCase(Locale.ROOT);
        if (s.contains("augsburger-allgemeine") || s.contains("augsburger allgemeine") || s.contains("immobilienbörse")) return "Augsburger Allgemeine / EXTRA";
        if (s.contains("wsb-bayern") || s.contains("wsb bayern")) return "WSB Bayern";
        if (s.contains("wohnbaugruppe")) return "Wohnbaugruppe Augsburg";
        if (s.contains("immomio") || s.contains("dawonia")) return "Dawonia / Immomio";
        if (s.contains("jaeger-wohnimmobilien") || s.contains("jäger wohnimmobilien")) return "JÄGER Wohnimmobilien";
        return "";
    }
    public static boolean mailApp(String pkg) {
        return pkg.equals("com.google.android.gm") || pkg.equals("com.microsoft.office.outlook") ||
                pkg.equals("com.samsung.android.email.provider") || pkg.equals("com.fsck.k9");
    }
    public static boolean isRelevantAlert(String pkg, String content) {
        String x = content.toLowerCase(Locale.ROOT);
        if (mailApp(pkg) && sourceFor(pkg, content).isEmpty()) return false;
        if (!mailApp(pkg) && !pkg.equals(SCOUT) && !pkg.equals(WELT) && !pkg.equals(KLEIN)) return false;
        boolean housing = hasAny(x, "wohnung", "immobil", "haus", "miet", "zimmer", "objekt", "exposé", "expose", "angebot");
        boolean newAlert = hasAny(x, "neu", "neue", "passend", "suchauftrag", "suchagent", "suchalarm",
                "treffer", "angebot", "inserat", "für deine suche", "zu deiner suche", "entspricht deiner suche");
        // Mail from a known sender is accepted only if it also concerns real estate.
        return housing && newAlert;
    }
    private static boolean hasAny(String text, String... tokens) {
        for (String s : tokens) if (text.contains(s)) return true;
        return false;
    }
    public static boolean definitelyFewerThanFourRooms(String text) {
        Matcher m = ROOMS.matcher(text == null ? "" : text);
        if (!m.find()) return false;
        try { return Double.parseDouble(m.group(1).replace(',', '.')) < 4.0; }
        catch (NumberFormatException e) { return false; }
    }
    public static String hostLabel(String url) {
        try { return URI.create(url).getHost().replaceFirst("^www\\.", ""); }
        catch (Exception e) { return "Externe Quelle"; }
    }
    public static String sha(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format(Locale.ROOT,"%02x", b & 0xff));
            return sb.toString();
        } catch (Exception e) { return String.valueOf(text.hashCode()); }
    }
}

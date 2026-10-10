import de.wohnungsradar.augsburg.ListingUtils;
public class TestListingUtils {
    static void check(boolean ok, String name) { if (!ok) throw new AssertionError(name); }
    public static void main(String[] args) {
        check("ImmoScout24".equals(ListingUtils.sourceFor(ListingUtils.SCOUT,"")), "scout source");
        check("immowelt".equals(ListingUtils.sourceFor(ListingUtils.WELT,"")), "welt source");
        check("Kleinanzeigen".equals(ListingUtils.sourceFor(ListingUtils.KLEIN,"")), "klein source");
        check("WSB Bayern".equals(ListingUtils.sourceFor("com.google.android.gm", "WSB Bayern Suchalarm")), "WSB mail source");
        check(ListingUtils.isRelevantAlert(ListingUtils.SCOUT,"Neue Wohnung passend zu deinem Suchauftrag"), "notification detect");
        check(!ListingUtils.isRelevantAlert("com.google.android.gm", "Neue E-Mail von Freundin"), "no unrelated mail");
        check(!ListingUtils.isRelevantAlert(ListingUtils.SCOUT,"Dein Passwort wurde geändert"), "no account notifications");
        check(ListingUtils.definitelyFewerThanFourRooms("3-Zimmerwohnung Augsburg"), "3 rooms rejected");
        check(ListingUtils.definitelyFewerThanFourRooms("2,5 Zimmer in Ulm"), "decimal 2.5 rejected");
        check(!ListingUtils.definitelyFewerThanFourRooms("4 Zimmer Augsburg"), "4 allowed");
        check(!ListingUtils.definitelyFewerThanFourRooms("Neue Wohnung Augsburg"), "unknown room should not be falsely rejected");
        check("https://example.org/id/123".equals(ListingUtils.firstUrl("Sieh https://example.org/id/123.")), "url parse");
        System.out.println("12 Hilfsfunktionen-Prüfungen erfolgreich.");
    }
}

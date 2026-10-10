package de.wohnungsradar.augsburg;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Optional direct monitoring of user-supplied official RSS/Atom feeds. */
public final class FeedSync {
    private FeedSync() {}
    public static final String PREFS = "wohnungsradar_settings";
    public static final String FEEDS = "rss_urls";

    public static String[] configured(Context ctx) {
        String raw = ctx.getSharedPreferences(PREFS, 0).getString(FEEDS, "");
        if (raw == null || raw.trim().isEmpty()) return new String[0];
        return raw.trim().split("\\n");
    }
    public static int fetchAll(Context context) {
        String[] feeds = configured(context);
        int added = 0;
        for (String url : feeds) {
            try { added += fetch(context, url.trim()); }
            catch (Exception ignored) { /* One broken feed must not stop the others. */ }
        }
        return added;
    }
    public static int fetch(Context ctx, String feedUrl) throws Exception {
        if (!feedUrl.startsWith("https://")) throw new IllegalArgumentException("HTTPS-Feed benötigt");
        URL endpoint = new URL(feedUrl);
        HttpURLConnection con = (HttpURLConnection) endpoint.openConnection();
        con.setConnectTimeout(9000); con.setReadTimeout(12000);
        con.setRequestProperty("User-Agent", "WohnungsRadar-Augsburg/0.2 (RSS reader)");
        con.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, text/xml, application/xml");
        con.setInstanceFollowRedirects(false);
        try {
            if (con.getResponseCode() != 200) return 0;
            String contentType = con.getContentType();
            if (contentType != null && contentType.toLowerCase().contains("html")) return 0;
            XmlPullParser p = Xml.newPullParser();
            p.setInput(con.getInputStream(), "UTF-8");
            List<Item> items = new ArrayList<>();
            boolean inItem = false;
            Item cur = null;
            int event;
            while ((event = p.next()) != XmlPullParser.END_DOCUMENT && items.size() < 80) {
                String name = p.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (name == null) continue;
                    if (name.equalsIgnoreCase("item") || name.equalsIgnoreCase("entry")) {
                        inItem = true; cur = new Item();
                    } else if (inItem && cur != null) {
                        if (name.equalsIgnoreCase("title")) cur.title = p.nextText();
                        else if (name.equalsIgnoreCase("link")) {
                            String href = p.getAttributeValue(null,"href");
                            if (href != null && href.startsWith("https://")) cur.link = href;
                            else { String t = p.nextText(); if (t.startsWith("https://")) cur.link = t; }
                        } else if (name.equalsIgnoreCase("description") || name.equalsIgnoreCase("summary")) {
                            cur.description = p.nextText().replaceAll("<[^>]*>", " ").trim();
                        }
                    }
                } else if (event == XmlPullParser.END_TAG && name != null &&
                        (name.equalsIgnoreCase("item") || name.equalsIgnoreCase("entry"))) {
                    if (cur != null && !cur.title.trim().isEmpty() && cur.link.startsWith("https://")) items.add(cur);
                    cur = null; inItem = false;
                }
            }
            String prefKey = "rss_initialized_" + ListingUtils.sha(feedUrl).substring(0,16);
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS,0);
            boolean initialized = prefs.getBoolean(prefKey,false);
            OfferDb db = new OfferDb(ctx);
            int count = 0;
            for (Item item : items) {
                if (ListingUtils.definitelyFewerThanFourRooms(item.title + " " + item.description)) continue;
                boolean fresh = db.add("rss:" + item.link,
                        ListingUtils.hostLabel(feedUrl),item.title,item.description,item.link,
                        "", "", System.currentTimeMillis());
                if (fresh) {
                    count++;
                    if (initialized) AlertChannels.show(ctx,ListingUtils.hostLabel(feedUrl),item.title,
                            item.description, (int)(System.nanoTime() % Integer.MAX_VALUE));
                }
            }
            prefs.edit().putBoolean(prefKey,true).apply();
            return count;
        } finally { con.disconnect(); }
    }
    private static class Item { String title="", link="", description=""; }
}

package de.wohnungsradar.augsburg;

import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.concurrent.ConcurrentHashMap;

/** Captures only supported housing alerts on this device, without web credentials. */
public class AlarmListenerService extends NotificationListenerService {
    private static final ConcurrentHashMap<String, PendingIntent> originals = new ConcurrentHashMap<>();

    static boolean openOriginal(String key) {
        if (key == null || key.isEmpty()) return false;
        PendingIntent pi = originals.get(key);
        if (pi == null) return false;
        try { pi.send(); return true; }
        catch (PendingIntent.CanceledException e) { originals.remove(key); return false; }
    }
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null || sbn.getPackageName().equals(getPackageName())) return;
        // Enabling permission may replay old notifications; ignore stale items.
        if (sbn.getPostTime() < System.currentTimeMillis() - 15 * 60 * 1000L) return;
        String pkg = sbn.getPackageName();
        if (!ListingUtils.mailApp(pkg) && !pkg.equals(ListingUtils.SCOUT) &&
                !pkg.equals(ListingUtils.WELT) && !pkg.equals(ListingUtils.KLEIN)) return;
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;
        String title = String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TITLE, ""));
        String message = String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TEXT, ""));
        String big = String.valueOf(n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT, ""));
        String content = (title + " " + message + " " + big).trim();
        if (!ListingUtils.isRelevantAlert(pkg, content)) return;
        // Zimmerfilter is primarily defined in the ORIGINAL searches. Only discard when certain.
        if (ListingUtils.definitelyFewerThanFourRooms(content)) return;
        String source = ListingUtils.sourceFor(pkg, content);
        String url = ListingUtils.firstUrl(content);
        // Notification keys are unique while displayed; approximate fallback for repeat alerts.
        String k = !url.isEmpty() ? "url:" + url : "push:" + ListingUtils.sha(pkg + "|" + title + "|" + message);
        String nKey = sbn.getKey();
        if (n.contentIntent != null) originals.put(nKey, n.contentIntent);
        boolean added = new OfferDb(this).add(k, source, title.isEmpty() ? "Neues Angebot" : title,
                message, url, pkg, nKey, System.currentTimeMillis());
        if (added) AlertChannels.show(this, source, title, message,
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE));
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn != null) originals.remove(sbn.getKey());
    }
}

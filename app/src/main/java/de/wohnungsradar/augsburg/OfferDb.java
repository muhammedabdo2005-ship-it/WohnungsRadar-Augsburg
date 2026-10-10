package de.wohnungsradar.augsburg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/** Private on-device storage. No data is sent to our own servers. */
public class OfferDb extends SQLiteOpenHelper {
    public static final class Offer {
        public long id, createdAt;
        public int contacted;
        public String key, source, title, body, url, packageName, notificationKey;
    }

    public OfferDb(Context context) { super(context, "wohnungsradar.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE offers (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_key TEXT UNIQUE NOT NULL, source TEXT NOT NULL, title TEXT NOT NULL, " +
                "body TEXT NOT NULL, url TEXT NOT NULL, package_name TEXT NOT NULL, " +
                "notification_key TEXT NOT NULL, created_at INTEGER NOT NULL, contacted INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_created ON offers(created_at DESC)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) { /* version 1 */ }

    /** Returns true only if this item was actually new. */
    public synchronized boolean add(String key, String source, String title, String body,
                                     String url, String pkg, String nKey, long time) {
        ContentValues values = new ContentValues();
        values.put("item_key", key); values.put("source", source);
        values.put("title", title); values.put("body", body);
        values.put("url", url); values.put("package_name", pkg);
        values.put("notification_key", nKey); values.put("created_at", time);
        long result = getWritableDatabase().insertWithOnConflict("offers", null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
        if (result != -1) {
            // Avoid growing indefinitely when long-term monitoring.
            getWritableDatabase().execSQL("DELETE FROM offers WHERE id NOT IN " +
                    "(SELECT id FROM offers ORDER BY created_at DESC LIMIT 1000)");
        }
        return result != -1;
    }

    public synchronized List<Offer> all() {
        List<Offer> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,item_key,source,title,body,url," +
                "package_name,notification_key,created_at,contacted FROM offers " +
                "ORDER BY created_at DESC LIMIT 250", null)) {
            while (c.moveToNext()) {
                Offer o = new Offer();
                o.id=c.getLong(0); o.key=c.getString(1); o.source=c.getString(2);
                o.title=c.getString(3); o.body=c.getString(4);
                o.url=c.getString(5); o.packageName=c.getString(6);
                o.notificationKey=c.getString(7); o.createdAt=c.getLong(8);
                o.contacted=c.getInt(9); out.add(o);
            }
        }
        return out;
    }
    public synchronized void markContacted(long id, boolean done) {
        ContentValues v = new ContentValues(); v.put("contacted", done ? 1 : 0);
        getWritableDatabase().update("offers", v, "id=?", new String[]{String.valueOf(id)});
    }
    public synchronized void delete(long id) {
        getWritableDatabase().delete("offers", "id=?", new String[]{String.valueOf(id)});
    }
}

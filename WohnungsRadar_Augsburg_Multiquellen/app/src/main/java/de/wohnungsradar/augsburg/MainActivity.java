package de.wohnungsradar.augsburg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Main screen, offer inbox and contact-template clipboard workflow. */
public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(13, 84, 116);
    private static final int BACKGROUND = Color.rgb(244, 248, 251);
    private static final String DEFAULT_TEMPLATE = "Guten Tag,\n\nich interessiere mich für Ihre angebotene Immobilie " +
            "und würde mich über eine Rückmeldung und die Möglichkeit einer Besichtigung freuen. " +
            "Gerne reiche ich bei Bedarf weitere Unterlagen nach.\n\nMit freundlichen Grüßen\n[Mein Name]";
    private LinearLayout root;
    private final String[] sourceNames = {
            "ImmoScout24 – Push", "immowelt – Push", "Kleinanzeigen – Push",
            "Augsburger Allgemeine / EXTRA – E-Mail", "WSB Bayern – Webseite",
            "Wohnbaugruppe Augsburg – Interessentenformular", "Dawonia – Immomio Suchprofil",
            "JÄGER Wohnimmobilien – Suchauftrag"};
    private final String[] sourceLinks = {
            "https://www.immobilienscout24.de/Suche/de/bayern/augsburg/wohnung-mieten",
            "https://www.immowelt.de/suche/augsburg/wohnungen/mieten",
            "https://www.kleinanzeigen.de/s-wohnung-mieten/augsburg/c203l7518",
            "https://immobilien.augsburger-allgemeine.de/",
            "https://wsb-bayern.de/mietangebote/",
            "https://www.wohnbaugruppe.de/leben/immobilien/interessentenformular",
            "https://www.dawonia.de/de/mieten",
            "https://jaeger-wohnimmobilien.de/suchauftrag/"};
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        prefs = getSharedPreferences(FeedSync.PREFS, 0);
        try {
            AlertChannels.ensure(this);
        } catch (RuntimeException e) {
            android.util.Log.e("WohnungsRadar", "Notification setup failed", e);
        }
        try {
            FeedJobService.schedule(this);
        } catch (RuntimeException e) {
            android.util.Log.e("WohnungsRadar", "Job scheduling failed", e);
        }
        // Recreation restores the existing screen; do not import its share again.
        if (savedState == null) handleSharedIntent(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); handleSharedIntent(intent); render();
    }
    @Override protected void onResume() { super.onResume(); render(); }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setBackgroundColor(BACKGROUND);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(20),dp(18),dp(24));
        scroll.addView(root); setContentView(scroll);
        TextView title = text("🏠 WohnungsRadar Augsburg", 24, BLUE, true); root.addView(title);
        root.addView(text("Alle Angebote an einem Ort · Suche: Augsburg + Umgebung · ab 4 Zimmer", 14, Color.DKGRAY, false));
        root.addView(text("Wichtig: Die Filter für Radius, Zimmer und Miete legst du in den Portalen fest. " +
                "Diese App sammelt deren neue Meldungen; sie durchsucht nicht automatisch alle Webseiten.", 12, Color.GRAY, false));

        Button access = button("1  Benachrichtigungszugriff freigeben", () ->
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        root.addView(access);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            root.addView(button("2  Eigene Push-Meldungen zulassen", () ->
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7)));
        }
        root.addView(button("Quellen einrichten (8 Anbieter)", this::showSources));
        root.addView(button("📝 Bewerbungstext speichern / ändern", this::editTemplate));
        root.addView(button("🔗 Inserat manuell hinzufügen", this::addLinkDialog));
        root.addView(button("📡 RSS-Feeds verwalten (optional)", this::editFeeds));
        root.addView(button("↻ RSS jetzt prüfen", this::refreshFeeds));

        List<OfferDb.Offer> offers = new OfferDb(this).all();
        int contacted = 0; for (OfferDb.Offer o : offers) if (o.contacted == 1) contacted++;
        root.addView(text("ANGEBOTE  (" + offers.size() + " gespeichert, " + contacted + " kontaktiert)", 18, BLUE, true));
        if (offers.isEmpty()) {
            root.addView(text("Noch keine Angebote gesammelt. Aktiviere oben den Zugriff, " +
                    "speichere Suchaufträge bei ImmoScout24, immowelt und Kleinanzeigen " +
                    "und schalte deren Push-Mitteilungen ein. Alternativ teile ein Inserat " +
                    "über die Android-Teilen-Funktion mit WohnungsRadar.", 15, Color.DKGRAY, false));
            return;
        }
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMANY);
        for (OfferDb.Offer o : offers) {
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.WHITE); card.setPadding(dp(12),dp(12),dp(12),dp(12));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1,-2);
            cardLp.topMargin=dp(12); root.addView(card,cardLp);
            card.addView(text(o.source + " · " + dateFormat.format(new Date(o.createdAt)) +
                    (o.contacted == 1 ? " · ✔ Kontaktiert" : ""), 12, BLUE, true));
            card.addView(text(o.title, 16, Color.BLACK, true));
            if (!o.body.trim().isEmpty()) card.addView(text(o.body.length()>220 ? o.body.substring(0,220)+"…" : o.body,
                    13, Color.DKGRAY, false));
            card.addView(button("Inserat öffnen", () -> openOffer(o)));
            card.addView(button("📋 Anschreiben kopieren + Inserat öffnen", () -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Bewerbung", template()));
                Toast.makeText(this,"Anschreiben kopiert – beim Anbieter einfügen und selbst absenden.",Toast.LENGTH_LONG).show();
                openOffer(o);
            }));
            card.addView(button(o.contacted==1 ? "↩ Kontaktierung zurücksetzen" : "✔ Als kontaktiert markieren", () -> {
                new OfferDb(this).markContacted(o.id, o.contacted!=1); render();
            }));
            Button del = button("Angebot löschen", () -> new AlertDialog.Builder(this)
                    .setTitle("Angebot entfernen?")
                    .setPositiveButton("Löschen",(d,w)->{new OfferDb(this).delete(o.id);render();})
                    .setNegativeButton("Abbrechen", null).show());
            card.addView(del);
        }
    }
    private String template() { return prefs.getString("contact_template", DEFAULT_TEMPLATE); }
    private void editTemplate() {
        EditText input = new EditText(this); input.setText(template()); input.setMinLines(7); input.setGravity(Gravity.TOP);
        new AlertDialog.Builder(this).setTitle("Mein Bewerbungsanschreiben")
                .setMessage("Der Text bleibt auf diesem Gerät. Er wird nie automatisch versendet.")
                .setView(input)
                .setPositiveButton("Speichern", (d,w)->{
                    prefs.edit().putString("contact_template",input.getText().toString()).apply();
                    Toast.makeText(this,"Gespeichert",Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Abbrechen",null).show();
    }
    private void showSources() {
        new AlertDialog.Builder(this).setTitle("Immobilienquellen")
                .setItems(sourceNames,(dialog,which)->openUrl(sourceLinks[which]))
                .setPositiveButton("Schließen",null).show();
    }
    private void addLinkDialog() {
        EditText input = new EditText(this); input.setHint("https://... (Link zum Inserat)"); input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
        new AlertDialog.Builder(this).setTitle("Inserat-Link hinzufügen").setView(input)
                .setPositiveButton("Speichern",(d,w)->{
                    String link=input.getText().toString().trim();
                    if (!link.startsWith("https://")) { toast("Bitte einen HTTPS-Link eingeben."); return; }
                    saveShared(link, "Manuell gespeichertes Inserat"); render();
                }).setNegativeButton("Abbrechen",null).show();
    }
    private void handleSharedIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        String text = "";
        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType())) {
            text=intent.getStringExtra(Intent.EXTRA_TEXT);
            String subject=intent.getStringExtra(Intent.EXTRA_SUBJECT);
            saveShared(text, subject);
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            saveShared(intent.getDataString(), "Geteiltes Immobilienangebot");
        }
        // Preserve the launch intent: Android and lifecycle observers use its identity.
    }
    private void saveShared(String content, String title) {
        if (content == null) return;
        String link = ListingUtils.firstUrl(content);
        if (link.isEmpty() && content.startsWith("https://")) link=content.trim();
        if (!link.startsWith("https://")) { toast("Kein gültiger HTTPS-Inserat-Link gefunden."); return; }
        if (title==null || title.isEmpty()) title="Geteiltes Immobilienangebot";
        boolean added=new OfferDb(this).add("share:"+link, ListingUtils.hostLabel(link),
                title,content,link,"","",System.currentTimeMillis());
        if (added) toast("Inserat gespeichert");
    }
    private void editFeeds() {
        EditText input = new EditText(this); input.setMinLines(6); input.setGravity(Gravity.TOP);
        input.setText(prefs.getString(FeedSync.FEEDS,""));
        input.setHint("https://beispiel.de/wohnungen.rss\nhttps://makler.de/feed.xml");
        new AlertDialog.Builder(this).setTitle("RSS/Atom-Feeds")
                .setMessage("Nur echte, vom Anbieter bereitgestellte RSS- oder Atom-Feed-Adressen. " +
                        "Eine normale Webseitenadresse funktioniert hier nicht. " +
                        "Android prüft ungefähr alle 15 Minuten (nicht sekundengenau).")
                .setView(input).setPositiveButton("Speichern",(d,w)->{
                    String raw=input.getText().toString().trim();
                    String[] parts=raw.isEmpty()?new String[0]:raw.split("\\n");
                    if (parts.length>10) {toast("Maximal 10 Feeds.");return;}
                    for(String line:parts) if(!line.trim().startsWith("https://")) {toast("Nur HTTPS-Feedadressen.");return;}
                    prefs.edit().putString(FeedSync.FEEDS,raw).apply();
                    FeedJobService.schedule(this); toast("RSS-Feeds gespeichert");
                }).setNegativeButton("Abbrechen",null).show();
    }
    private void refreshFeeds() {
        toast("RSS-Abfrage gestartet …");
        new Thread(() -> {
            int n=FeedSync.fetchAll(getApplicationContext());
            runOnUiThread(()->{toast(n+" neue Einträge importiert");render();});
        },"FeedManualRefresh").start();
    }
    private void openOffer(OfferDb.Offer o) {
        if (!o.notificationKey.isEmpty() && AlarmListenerService.openOriginal(o.notificationKey)) return;
        if (!o.url.isEmpty()) {openUrl(o.url);return;}
        if (!o.packageName.isEmpty()) {
            Intent launcher=getPackageManager().getLaunchIntentForPackage(o.packageName);
            if (launcher != null) {startActivity(launcher);return;}
        }
        int i=o.source.contains("Scout")?0:o.source.contains("immowelt")?1:
                o.source.contains("Kleinanzeigen")?2:o.source.contains("Augsburger")?3:-1;
        if(i>=0) {openUrl(sourceLinks[i]); toast("Direktlink nicht verfügbar; bitte Angebot in der Original-App suchen.");}
        else toast("Kein Direktlink verfügbar. Bitte über den Anbieter suchen.");
    }
    private void openUrl(String url) {
        try {startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE));}
        catch (Exception e) {toast("Link konnte nicht geöffnet werden.");}
    }
    private Button button(String label,Runnable fn) {
        Button btn=new Button(this); btn.setText(label);btn.setAllCaps(false);
        btn.setTextColor(BLUE);btn.setOnClickListener(v->fn.run());return btn;
    }
    private TextView text(String content,int size,int color,boolean bold) {
        TextView t=new TextView(this);t.setText(content);t.setTextSize(size);t.setTextColor(color);
        t.setPadding(0,dp(6),0,dp(5));
        if(bold)t.setTypeface(null,1);
        return t;
    }
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
    private void toast(String txt){Toast.makeText(this,txt,Toast.LENGTH_LONG).show();}
}

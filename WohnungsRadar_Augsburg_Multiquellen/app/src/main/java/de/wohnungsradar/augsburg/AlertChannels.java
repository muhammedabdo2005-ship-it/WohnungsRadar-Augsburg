package de.wohnungsradar.augsburg;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public final class AlertChannels {
    private AlertChannels() {}
    public static final String CHANNEL = "housing_alerts_v2";
    public static void ensure(Context ctx) {
        NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel(CHANNEL, "Neue Immobilienangebote", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Neue Angebote aus eingebundenen Suchaufträgen oder RSS-Feeds");
        manager.createNotificationChannel(ch);
    }
    public static void show(Context ctx, String source, String title, String body, int id) {
        if (Build.VERSION.SDK_INT >= 33 && ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        ensure(ctx);
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, id, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification n = new Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setContentTitle("🏠 " + source + ": " + title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setContentIntent(pi).setAutoCancel(true).build();
        ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, n);
    }
}

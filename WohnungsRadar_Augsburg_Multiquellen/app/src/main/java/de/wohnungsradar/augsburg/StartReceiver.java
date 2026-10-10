package de.wohnungsradar.augsburg;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class StartReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) FeedJobService.schedule(context);
    }
}

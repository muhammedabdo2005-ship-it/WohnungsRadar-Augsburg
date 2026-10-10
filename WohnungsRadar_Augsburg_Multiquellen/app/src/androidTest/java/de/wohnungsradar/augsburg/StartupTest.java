package de.wohnungsradar.augsburg;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Runs against the real Android scheduler, not a mock that ignores permissions. */
@RunWith(AndroidJUnit4.class)
public class StartupTest {
    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test public void networkJobCanBeScheduled() {
        Context context = context();
        assertEquals(PackageManager.PERMISSION_GRANTED,
                context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE));
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        assertNotNull(scheduler);
        scheduler.cancelAll();
        // A missing manifest permission throws on API 34+, even if the APK builds.
        FeedJobService.schedule(context);
        JobInfo job = scheduler.getPendingJob(6481);
        assertNotNull("RSS job must actually be registered", job);
        assertTrue(job.isPersisted());
        assertTrue(job.isPeriodic());
        assertEquals(JobInfo.NETWORK_TYPE_ANY, job.getNetworkType());
    }

    @Test public void launcherStartsAndRecreatesWithoutNotificationPermission() {
        Context context = context();
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            assertEquals("Run on a fresh test installation with notifications ungranted",
                    PackageManager.PERMISSION_DENIED,
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS));
        }
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(
                    activity.findViewById(android.R.id.content)));
            scenario.recreate();
            scenario.onActivity(activity -> assertFalse(activity.isFinishing()));
        }
    }

    @Test public void sharedOfferSurvivesStartupAndRecreation() {
        String url = "https://example.org/startup-regression";
        Intent intent = new Intent(context(), MainActivity.class)
                .setAction(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, url)
                .putExtra(Intent.EXTRA_SUBJECT, "Startup regression")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.recreate();
            try (OfferDb db = new OfferDb(context())) {
                long count = db.all().stream().filter(offer -> url.equals(offer.url)).count();
                assertEquals("Shared offer should be stored exactly once", 1L, count);
            }
        }
    }
}

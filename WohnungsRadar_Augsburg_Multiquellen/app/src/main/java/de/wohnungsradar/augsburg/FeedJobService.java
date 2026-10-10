package de.wohnungsradar.augsburg;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/** Periodic RSS/Atom feed scan where a site supplies such a feed. */
public class FeedJobService extends JobService {
    private static final int JOB_ID = 6481;
    public static void schedule(Context ctx) {
        JobScheduler js = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(ctx, FeedJobService.class))
                .setPeriodic(15 * 60 * 1000L)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true).build();
        js.schedule(job);
    }
    @Override public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try { FeedSync.fetchAll(getApplicationContext()); }
            finally { jobFinished(params, false); }
        }, "WohnungsRadarFeedSync").start();
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) { return true; }
}

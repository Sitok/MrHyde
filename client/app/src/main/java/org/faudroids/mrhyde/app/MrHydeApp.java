package org.faudroids.mrhyde.app;


import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.karumi.dexter.Dexter;

import org.faudroids.mrhyde.BuildConfig;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.bitbucket.BitbucketModule;
import org.faudroids.mrhyde.git.GitModule;
import org.faudroids.mrhyde.github.GitHubModule;
import org.faudroids.mrhyde.jekyll.JekyllModule;

import timber.log.Timber;

public class MrHydeApp extends Application {

  private AppComponent component;
  private Tracker analyticsTracker;

  @Override
  public void onCreate() {
    super.onCreate();

    // setup dependency injection
    component = DaggerAppComponent.builder()
        .appModule(new AppModule(this))
        .gitHubModule(new GitHubModule())
        .jekyllModule(new JekyllModule())
        .gitModule(new GitModule())
        .bitbucketModule(new BitbucketModule())
        .build();

    // setup google analytics
    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    analyticsTracker = analytics.newTracker(R.xml.global_tracker);
    analyticsTracker.enableAutoActivityTracking(true);
    analyticsTracker.enableExceptionReporting(true);

    // setup logging
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
      analytics.setAppOptOut(true);
    } else {
      Timber.plant(new CrashReportingTree());
    }

    // setup permission requests
    Dexter.initialize(this);
  }

  public AppComponent getComponent() {
    return component;
  }

  public Tracker getAnalyticsTracker() {
    return analyticsTracker;
  }

  private final class CrashReportingTree extends Timber.HollowTree {

    private final Timber.DebugTree debugTree = new Timber.DebugTree();

    @Override
    public void e(String message, Object... args) {
      debugTree.e(message, args);
    }

    @Override
		public void e(Throwable e, String msg, Object... args) {
      debugTree.e(e, msg, args);
      reportThrowable(e);
		}

    @Override
    public void w(String message, Object... args) {
      debugTree.w(message);
    }

		@Override
		public void w(Throwable e, String msg, Object... args) {
      debugTree.w(e, msg, args);
      reportThrowable(e);
		}

    private void reportThrowable(Throwable e) {
      analyticsTracker.send(new HitBuilders.ExceptionBuilder()
          .setDescription(new StandardExceptionParser(MrHydeApp.this, null)
              .getDescription(Thread.currentThread().getName(), e))
          .setFatal(false)
          .build()
      );
    }

  }
}

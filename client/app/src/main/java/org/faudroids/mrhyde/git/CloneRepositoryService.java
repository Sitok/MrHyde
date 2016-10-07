package org.faudroids.mrhyde.git;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.utils.DefaultTransformer;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * {@link android.app.Service} for cloning remote repositories to the device.
 */
public class CloneRepositoryService extends Service {

  public static final String EXTRA_REPOSITORY = "EXTRA_REPOSITORY";
  public static final String EXTRA_IMPORT_PRE_V1_REPOSITORY = "EXTRA_IMPORT_PRE_V1_REPOSITORY";
  public static final String EXTRA_NOTIFICATION_TARGET_ACTIVITY = "EXTRA_NOTIFICATION_TARGET_ACTIVITY";

  private static final int NOTIFICATION_ID = 42;

  // hack to check if service is running
  // http://stackoverflow.com/a/608600
  private static boolean isRunning = false;

  @Inject protected RepositoriesManager repositoriesManager;
  @Inject protected NotificationManager notificationManager;

  private BehaviorSubject<GitManager> cloneStatus;
  private Repository repository;

  @Override
  public void onCreate() {
    super.onCreate();
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    isRunning = true;
  }

  @Override
  public void onDestroy() {
    isRunning = false;
    notificationManager.cancel(NOTIFICATION_ID);
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    repository = (Repository) intent.getSerializableExtra(EXTRA_REPOSITORY);
    boolean importPreV1Repo = intent.getBooleanExtra(EXTRA_IMPORT_PRE_V1_REPOSITORY, false);
    Class<?> notificationTargetActivity = (Class<?>) intent.getSerializableExtra(EXTRA_NOTIFICATION_TARGET_ACTIVITY);
    cloneStatus = BehaviorSubject.create();

    showCloneNotification(notificationTargetActivity);
    repositoriesManager
        .cloneRepository(repository, importPreV1Repo)
        .compose(new DefaultTransformer<>())
        .subscribe(
            gitManager -> {
              cloneStatus.onNext(gitManager);
              cloneStatus.onCompleted();
              stopSelf();
            },
            error -> {
              cloneStatus.onError(error);
              stopSelf();
            }
        );

    return START_STICKY;
  }

  private void showCloneNotification(Class<?> targetClass) {
    PendingIntent pendingIntent = PendingIntent.getActivity(
        getApplicationContext(),
        0,
        new Intent(getApplicationContext(), targetClass),
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setContentTitle(getString(R.string.clone_status_title, repository.getName()))
        .setContentText(getString(R.string.clone_status_message))
        .setContentIntent(pendingIntent)
        .setSmallIcon(R.drawable.ic_notification)
        .build();

    notificationManager.notify(NOTIFICATION_ID, notification);
  }


  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return new CloneStatusBinder(cloneStatus, repository);
  }

  public static class CloneStatusBinder extends Binder {

    private final Observable<GitManager> cloneStatus;
    private final Repository repository;

    public CloneStatusBinder(Observable<GitManager> cloneStatus, Repository repository) {
      this.cloneStatus = cloneStatus;
      this.repository = repository;
    }

    public Observable<GitManager> getCloneStatus() {
      return cloneStatus;
    }

    public Repository getRepository() {
      return repository;
    }

  }

  public static boolean isRunning() {
    return isRunning;
  }

}

package org.faudroids.mrhyde.app;

import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

  private final MrHydeApp app;

  public AppModule(MrHydeApp app) {
    this.app = app;
  }

  @Provides
  @Singleton
  Context provideApplicationContext() {
    return app;
  }

  @Provides
  @Singleton
  InputMethodManager provideInputMethodManager(Context context) {
    return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
  }

  @Provides
  @Singleton
  NotificationManager provideNotificationManager(Context context) {
    return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Provides
  @Singleton
  ClipboardManager provideClipboardManager(Context context) {
    return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
  }

}

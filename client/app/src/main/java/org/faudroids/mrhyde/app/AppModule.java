package org.faudroids.mrhyde.app;

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

}

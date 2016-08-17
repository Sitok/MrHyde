package org.faudroids.mrhyde.jekyll;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class JekyllModule {

  @Provides
  @Singleton
  public JekyllApi provideJekyllApi() {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint("https://faudroid.markab.uberspace.de")
        .build();
    return restAdapter.create(JekyllApi.class);
  }

}

package org.faudroids.mrhyde.bitbucket;

import android.content.Context;
import android.util.Base64;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.auth.LoginManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module
public class BitbucketModule {

  @Provides
  @Singleton
  public BitbucketAuthApi provideBitbucketAuthApi(Context context) {
    return new RestAdapter.Builder()
        .setEndpoint("https://bitbucket.org")
        .setRequestInterceptor(request -> {
          String clientId = context.getString(R.string.bitbucketClientId);
          String clientSecret = context.getString(R.string.bitbucketClientSecret);
          request.addHeader("Authorization", "Basic " + Base64.encodeToString((clientId + ":" + clientSecret).getBytes(), Base64.NO_WRAP));
        })
        .build()
        .create(BitbucketAuthApi.class);
  }

  @Provides
  @Singleton
  public BitbucketGeneralApi provideBitbucketGeneralApi(LoginManager loginManager) {
    return new RestAdapter.Builder()
        .setEndpoint("https://api.bitbucket.org/2.0")
        .setRequestInterceptor(request -> {
          String accessToken = loginManager.getBitbucketAccount().getAccessToken();
          request.addHeader("Authorization", "Bearer " + accessToken);
        })
        .build()
        .create(BitbucketGeneralApi.class);
  }

}

package org.faudroids.mrhyde.utils;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.exceptions.OnErrorThrowable;

/**
 * Various helper methods for dealing with {@link Observable}.
 */
public class ObservableUtils {

  public interface Func<T> {
    T call() throws Exception;
  }

  public static <T> Observable<T> fromSynchronousCall(@NonNull Func<T> func) {
    return Observable.defer(() -> {
      try {
        return Observable.just(func.call());
      } catch (Exception e) {
        throw OnErrorThrowable.from(e);
      }
    });
  }

}

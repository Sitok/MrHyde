package org.faudroids.mrhyde.ui.utils;

import android.content.Context;
import android.net.Uri;

import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import rx.Observable;

public class ImageUtils {

  private final Context context;

  @Inject
  ImageUtils(Context context) {
    this.context = context;
  }


  /**
   * Loads an image from a possibly remote URI and writes the result to a file.
   */
  public Observable<Void> loadAndSaveFile(Uri imageUri, File targetFile) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      InputStream inStream = context.getContentResolver().openInputStream(imageUri);
      FileOutputStream outStream = new FileOutputStream(targetFile);
      int readLength;
      byte[] data = new byte[256];
      while ((readLength = inStream.read(data, 0, data.length)) != -1) {
        outStream.write(data, 0, readLength);
      }
      inStream.close();
      outStream.close();
      return null;
    });
  }

}

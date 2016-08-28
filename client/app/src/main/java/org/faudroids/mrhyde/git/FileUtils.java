package org.faudroids.mrhyde.git;

import android.support.annotation.NonNull;

import org.faudroids.mrhyde.utils.ObservableUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

/**
 * Helper methods for dealing with files.
 */
public class FileUtils {

	private static final int FIRST_FEW_BYTES = 8000;

	@Inject
	FileUtils() { }


  /**
   * Checks the file extension for common image variants.
   */
	public boolean isImage(@NonNull String fileName) {
		fileName = fileName.toLowerCase();
		return (fileName.endsWith(".png")
				|| fileName.endsWith(".jpg")
				|| fileName.endsWith(".jpeg")
				|| fileName.endsWith(".bmp")
				|| fileName.endsWith(".gif"));
	}


	/**
	 * Checks if a file is binary by scanning the max first few bytes and searching for a NUL byte.
	 * Courtesy to http://stackoverflow.com/a/6134127
	 */
	public boolean isBinary(@NonNull File file) throws IOException {
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int count = 0;
		while (count < FIRST_FEW_BYTES) {
			try {
				byte data = in.readByte();
				if (data == 0) return true;
			} catch (EOFException eof) {
        return false;
      }
      ++count;
    }
    return false;
  }


  /**
   * Deletes a file or directory recursively.
   */
  public Observable<Void> deleteFile(@NonNull File file) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      if (file.isDirectory()) {
        for (File f : file.listFiles()) {
          deleteFile(f);
        }
      }
      if (!file.delete()) {
        Timber.w("Failed to delete \"%s\"", file.getAbsolutePath());
      }
      return null;
    });
  }


  /**
   * Returns the whole content of the given file as a string.
   */
  public Observable<String> readFile(@NonNull File file) {
    return ObservableUtils.fromSynchronousCall(() -> {
      StringBuilder builder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
        builder.append('\n');
      }
      reader.close();
      return builder.toString();
    });
  }


  /**
   * Writes content to a file.
   */
  public Observable<Void> writeFile(@NonNull File file, @NonNull String content) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      FileOutputStream writer = new FileOutputStream(file);
      writer.write(content.getBytes());
      writer.close();
      return null;
    });
  }


  /**
   * Renames a file.
   */
  public Observable<Void> renameFile(@NonNull File file, @NonNull File targetFile) {
    return renameFile(file, targetFile, false);
  }


  public Observable<Void> renameFile(@NonNull  File file, @NonNull File targetFile, boolean overwrite) {
    Observable<Void> renameObservable = ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      if (!file.renameTo(targetFile)) {
        Timber.w("Failed to rename file %s to %s", file.getAbsolutePath(), targetFile.getAbsolutePath());
      }
      return null;
    });

    if (overwrite && !targetFile.exists()) return renameObservable;
    return deleteFile(file)
        .flatMap(nothing -> renameObservable);
  }

  /**
   * Creates a single new plain file.
   */
  public Observable<Void> createNewFile(@NonNull File file) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      if (!file.createNewFile()) Timber.w("Failed to create file %s", file.getAbsolutePath());
      return null;
    });
  }


  /**
   * Creates a single new empty directory.
   */
  public Observable<Void> createNewDirectory(@NonNull File dir) {
    return ObservableUtils.fromSynchronousCall((ObservableUtils.Func<Void>) () -> {
      if (!dir.mkdir()) Timber.w("Failed to create directory %s", dir.getAbsolutePath());
      return null;
    });
  }

}

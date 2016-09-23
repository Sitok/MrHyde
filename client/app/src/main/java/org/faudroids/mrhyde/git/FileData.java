package org.faudroids.mrhyde.git;


import com.google.common.base.Objects;

import java.io.File;
import java.io.Serializable;

public class FileData implements Serializable {

  private final File file;
  private final byte[] data;

  public FileData(File file, byte[] data) {
    this.file = file;
    this.data = data;
  }

  public File getFile() {
    return file;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FileData)) return false;
    FileData fileData = (FileData) o;
    return Objects.equal(file, fileData.file) &&
        Objects.equal(data, fileData.data);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(file, data);
  }
}

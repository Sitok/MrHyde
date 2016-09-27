package org.faudroids.mrhyde.git;

import android.content.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Optional;
import com.snappydb.DB;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class GitModule {

  public static final String DB_REPOSITORIES_NAME = "repositories";

  @Provides
  @Named(DB_REPOSITORIES_NAME)
  @Singleton
  public DB provideRepositoryDb(Context context) {
    try {
      return new SnappyDB
          .Builder(context)
          .name(DB_REPOSITORIES_NAME)
          // This might be slow. Better not clone too many repositories ;)
          // Kryto requires no args constructors and cannot serialize guava Optionals :(
          .registerSerializers(Repository.class, new Serializer<Repository>() {
            @Override
            public void write(Kryo kryo, Output output, Repository repository) {
              output.writeString(repository.getName());
              output.writeString(repository.getCloneUrl());
              RepositoryOwner owner = repository.getOwner().orNull();
              if (owner != null) {
                output.writeString(owner.getUsername());
                output.writeString(owner.getAvatarUrl().orNull());
              } else {
                output.writeString(null);
                output.writeString(null);
              }
            }
            @Override
            public Repository read(Kryo kryo, Input input, Class<Repository> type) {
              String name = input.readString();
              String cloneUrl = input.readString();

              String username = input.readString();
              String avatarUrl = input.readString();
              RepositoryOwner owner = null;
              if (username != null) owner = new RepositoryOwner(username, Optional.fromNullable(avatarUrl));

              return new Repository(name, cloneUrl, Optional.fromNullable(owner));
            }
          })
          .build();
    } catch (SnappydbException e) {
      Timber.d(e, "Failed to open DB");
      // TODO what to do here??
      throw new RuntimeException(e);
    }
  }
}

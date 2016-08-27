package org.faudroids.mrhyde.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ortiz.touch.TouchImageView;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.AbstractActionBarActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ImageViewerActivity extends AbstractActionBarActivity {

  static final String
      EXTRA_REPOSITORY = "EXTRA_REPOSITORY",
      EXTRA_FILE = "EXTRA_FILE";

  @BindView(R.id.image) protected TouchImageView imageView;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_viewer);
    ButterKnife.bind(this);

    // load arguments
    File file = (File) getIntent().getSerializableExtra(EXTRA_FILE);

    // load image
    setTitle(file.getName());
    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    imageView.setImageBitmap(bitmap);
    imageView.setZoom(0.999999f); // hack, if not present image is sometimes empty
  }

}

package org.faudroids.mrhyde.ui;

import android.content.Intent;
import android.os.Bundle;

import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;


/**
 * Forward to {@link LoginActivity}.
 */
public final class SplashScreenActivity extends AbstractActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    ((MrHydeApp) getApplication()).getComponent().inject(this);

    startActivity(new Intent(this, LoginActivity.class));
    finish();
	}


}

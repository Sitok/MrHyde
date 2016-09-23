package org.faudroids.mrhyde.utils;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.faudroids.mrhyde.R;

import java.net.UnknownHostException;

import retrofit.RetrofitError;
import timber.log.Timber;

public class DefaultErrorAction extends AbstractErrorAction {

	private final Context context;
	private final String logMessage;

	public DefaultErrorAction(Context context, String logMessage) {
		this.context = context;
		this.logMessage = logMessage;
	}

	@Override
	protected void doCall(Throwable throwable) {
		if ((throwable instanceof RetrofitError && ((RetrofitError) throwable).getKind().equals(RetrofitError.Kind.NETWORK))
				|| (throwable instanceof UnknownHostException)) {

			// network problems
			Timber.d(throwable, logMessage);
			new MaterialDialog.Builder(context)
					.title(R.string.error_network_title)
					.content(R.string.error_network_message)
					.positiveText(android.R.string.ok)
					.show();

		} else {
			// default internal message
			Timber.e(throwable, logMessage);
			MaterialDialog dialog = new MaterialDialog.Builder(context)
					.title(R.string.error_internal_title)
					.positiveText(android.R.string.ok)
          .customView(R.layout.dialog_internal_error, true)
          .show();

			View messageView = dialog.getCustomView();
			View expandDetailsView = messageView.findViewById(R.id.details_expand);
			final TextView detailsTextView = (TextView) messageView.findViewById(R.id.details);
			detailsTextView.setText(Log.getStackTraceString(throwable));

			expandDetailsView.setOnClickListener(v -> {
        // toggle details
        if (detailsTextView.getVisibility() == View.GONE) {
          detailsTextView.setVisibility(View.VISIBLE);
        } else {
          detailsTextView.setVisibility(View.GONE);
        }

      });
		}

	}

}

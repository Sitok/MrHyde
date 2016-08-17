package org.faudroids.mrhyde.ui.utils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.faudroids.mrhyde.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;


public abstract class AbstractFragment extends Fragment {

	private final int layoutResource;
	protected CompositeSubscription compositeSubscription = new CompositeSubscription();
	@Inject protected UiUtils uiUtils;
	@BindView(R.id.spinner) protected View spinnerContainerView;
	@BindView(R.id.spinner_image) protected ImageView spinnerImageView;

	protected AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(layoutResource, container, false);
    ButterKnife.bind(this, view);
    return view;
	}


	@Override
	public void onDestroy() {
		compositeSubscription.unsubscribe();
		compositeSubscription = new CompositeSubscription();
		super.onDestroy();
	}


	public void showSpinner() {
		uiUtils.showSpinner(spinnerContainerView, spinnerImageView);
	}


	public void hideSpinner() {
		uiUtils.hideSpinner(spinnerContainerView, spinnerImageView);
	}


	public boolean isSpinnerVisible() {
		return uiUtils.isSpinnerVisible(spinnerContainerView);
	}

}

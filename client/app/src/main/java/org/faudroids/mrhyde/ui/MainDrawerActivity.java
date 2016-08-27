package org.faudroids.mrhyde.ui;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.github.GitHubManager;
import org.faudroids.mrhyde.github.LoginManager;

import javax.inject.Inject;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import timber.log.Timber;


public class MainDrawerActivity extends MaterialNavigationDrawer<Fragment> {

  @Inject LoginManager loginManager;
  @Inject GitHubManager gitHubManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate");
  }

  @Override
  public void init(Bundle savedInstanceState) {
    Timber.d("init");
    ((MrHydeApp) getApplication()).getComponent().inject(this);

    // repositories
    addSection(newSection(getString(R.string.section_favourite_repositories), R.drawable.ic_heart_white, new FavouriteReposFragment()));
    addSection(newSection(getString(R.string.section_all_repositories), R.drawable.ic_list, new AllReposFragment()));

    // show favourites repo per default if not empty
    if (gitHubManager.hasFavouriteRepositories()) setDefaultSectionLoaded(0);
    else setDefaultSectionLoaded(1);

    //account information
    LoginManager.Account account = loginManager.getAccount();
    addAccount(new MaterialAccount(
        getResources(),
        account.getLogin(),
        account.getEmail(),
        account.getAvatar(),
        R.drawable.drawer_background));
    setBackPattern(MaterialNavigationDrawer.BACKPATTERN_BACK_TO_FIRST);

    //settings and feedback
    this.addBottomSection(newSection(getString(R.string.section_settings), R.drawable.ic_settings, new SettingsFragment()));

    String address = getString(R.string.feedback_mail_address);
    String subject = getString(R.string.feedback_mail_subject);
    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    Intent mailer = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
    this.addBottomSection(newSection(getString(R.string.section_feedback), R.drawable.ic_email, mailer));
  }

}

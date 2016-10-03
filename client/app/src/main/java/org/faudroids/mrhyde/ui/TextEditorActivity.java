package org.faudroids.mrhyde.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.Lists;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.app.MrHydeApp;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.Repository;
import org.faudroids.mrhyde.ui.utils.AbstractActivity;
import org.faudroids.mrhyde.ui.utils.UndoRedoEditText;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action0;
import timber.log.Timber;

public final class TextEditorActivity extends AbstractActivity {

  private static final int EDITOR_MAX_HISTORY = 100;

  static final String
      EXTRA_REPOSITORY = "EXTRA_REPOSITORY",
      EXTRA_FILE = "EXTRA_FILE",
      EXTRA_IS_NEW_FILE = "EXTRA_IS_NEW_FILE";

  private static final String
      STATE_SAVED_FILE_CONTENT = "STATE_SAVED_FILE_CONTENT",
      STATE_EDIT_TEXT = "STATE_EDIT_TEXT",
      STATE_EDIT_MODE = "STATE_EDIT_MODE",
      STATE_UNDO_REDO = "STATE_UNDO_REDO";

  private static final String
      PREFS_NAME = TextEditorActivity.class.getSimpleName();

  private static final String
      KEY_SHOW_LINE_NUMBERS = "KEY_SHOW_LINE_NUMBERS";

  @Inject ActivityIntentFactory intentFactory;
  @Inject InputMethodManager inputMethodManager;

  @BindView(R.id.content) protected EditText editText;
  private UndoRedoEditText undoRedoEditText;

  @BindView(R.id.edit) protected FloatingActionButton editButton;
  @BindView(R.id.line_numbers) protected TextView numLinesTextView;

  @Inject FileUtils fileUtils;
  private Repository repository;
  private File file;
  private String savedFileContent; // last saved content
  private boolean showingLineNumbers;


  @Override
  public void onCreate(final Bundle savedInstanceState) {
    ((MrHydeApp) getApplication()).getComponent().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_text_editor);
    ButterKnife.bind(this);

    // load arguments
    final boolean isNewFile = getIntent().getBooleanExtra(EXTRA_IS_NEW_FILE, false);
    file = (File) getIntent().getSerializableExtra(EXTRA_FILE);
    repository = (Repository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);
    setTitle(file.getName());

    // hide line numbers by default
    showingLineNumbers = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SHOW_LINE_NUMBERS, false);

    // start editing on long click
    editText.setOnLongClickListener(v -> {
      if (isEditMode()) return false;
      startEditMode();
      return true;
    });

    // setup line numbers
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        updateLineNumbers();
      }
    });

    // setup edit button
    editButton.setOnClickListener(v -> startEditMode());

    // setup undo / redo
    undoRedoEditText = new UndoRedoEditText(editText);
    undoRedoEditText.setMaxHistorySize(EDITOR_MAX_HISTORY);

    // load selected file
    if (savedInstanceState != null && savedInstanceState.getSerializable(STATE_SAVED_FILE_CONTENT) != null) {
      editText.post(() -> {
        savedFileContent = savedInstanceState.getString(STATE_SAVED_FILE_CONTENT);
        boolean startEditMode = savedInstanceState.getBoolean(STATE_EDIT_MODE);
        EditTextState editTextState = (EditTextState) savedInstanceState.getSerializable(STATE_EDIT_TEXT);
        showContent(startEditMode, editTextState);
        undoRedoEditText.restoreInstanceState(savedInstanceState, STATE_UNDO_REDO);
      });

    } else {
      loadContent(isNewFile);
    }

  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(STATE_SAVED_FILE_CONTENT, savedFileContent);
    outState.putBoolean(STATE_EDIT_MODE, isEditMode());
    EditTextState editTextState = new EditTextState(editText.getText().toString(), editText.getSelectionStart());
    outState.putSerializable(STATE_EDIT_TEXT, editTextState);
    undoRedoEditText.saveInstanceState(outState, STATE_UNDO_REDO);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_text_editor, menu);
    return true;
  }


  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem lineItem = menu.findItem(R.id.action_show_line_numbers);
    if (showingLineNumbers) lineItem.setChecked(true);
    else lineItem.setChecked(false);

    // toggle undo / redo buttons
    if (!isEditMode()) {
      menu.findItem(R.id.action_undo).setVisible(false);
      menu.findItem(R.id.action_redo).setVisible(false);
      menu.findItem(R.id.action_insert_markdown).setVisible(false);
    }

    return true;
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (isEditMode()) stopEditMode();
        else onBackPressed();
        return true;

      case R.id.action_show_line_numbers:
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
        toggleLineNumbers();
        return true;

      case R.id.action_undo:
        if (undoRedoEditText.getCanUndo()) {
          undoRedoEditText.undo();
        } else {
          Toast.makeText(this, getString(R.string.nothing_to_undo), Toast.LENGTH_SHORT).show();
        }
        return true;

      case R.id.action_redo:
        if (undoRedoEditText.getCanRedo()) {
          undoRedoEditText.redo();
        } else {
          Toast.makeText(this, getString(R.string.nothing_to_redo), Toast.LENGTH_SHORT).show();
        }
        return true;

      case R.id.action_preview:
        saveFile();
        startActivity(intentFactory.createPreviewIntent(repository));
        return true;

			case R.id.action_insert_markdown:
				showMarkdownInsertionDialog();
				return true;
    }
    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onBackPressed() {
    if (isEditMode()) {
      if (!isDirty()) {
        returnResult();

      } else {
        new MaterialDialog.Builder(this)
            .title(R.string.save_title)
            .content(R.string.save_message)
            .cancelable(false)
            .positiveText(R.string.save_ok)
            .onPositive((dialog, which) -> {
              saveFile();
              returnResult();
            })
            .negativeText(R.string.save_cancel)
            .onNegative((dialog, which) -> returnResult())
            .show();
      }
    } else {
      returnResult();
    }
  }


  private void loadContent(final boolean isNewFile) {
    showSpinner();
    compositeSubscription.add(fileUtils
        .readFile(file)
        .compose(new DefaultTransformer<>())
        .subscribe(
            content -> {
              hideSpinner();
              try {
                savedFileContent = new String(content, "UTF-8");
                Timber.d(savedFileContent);
              } catch (UnsupportedEncodingException e) {
                Timber.e(e, "Encoding not supported");
              }
              showContent(isNewFile, null);
            },
            new ErrorActionBuilder()
                .add(new DefaultErrorAction(this, "Failed to read file"))
                .add(new HideSpinnerAction(this))
                .build()
        )
    );
  }


  private void showContent(boolean startEditMode, @Nullable EditTextState editTextState) {
    // set text
    if (editTextState != null) editText.setText(editTextState.text);
    else editText.setText(savedFileContent);
    undoRedoEditText.clearHistory(); // otherwise setting this text would be part of history
    editText.setTypeface(Typeface.MONOSPACE);

    // start edit mode
    if (startEditMode) startEditMode();
    else stopEditMode();

    // restore cursor position
    if (editTextState != null) {
      editText.setSelection(editTextState.cursorPosition);
    }

    updateLineNumbers();
  }


  private void saveFile() {
    Timber.d("saving file");
    fileUtils
        .writeFile(file, editText.getText().toString())
        .compose(new DefaultTransformer<>())
        .subscribe(
            nothing -> {
            },
            new ErrorActionBuilder()
                .add(new DefaultErrorAction(this, "Failed to write file"))
                .build()
        );
  }


  /**
   * @return true if the file has been changed
   */
  private boolean isDirty() {
    return savedFileContent != null && !savedFileContent.equals(editText.getText().toString());
  }


  private void returnResult() {
    setResult(RESULT_OK);
    finish();
  }


  private void startEditMode() {
    editText.setFocusable(true);
    editText.setFocusableInTouchMode(true);
    editText.requestFocus();
    editButton.setVisibility(View.GONE);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done);
    inputMethodManager.showSoftInput(editText, 0);
    invalidateOptionsMenu();
  }


  private void stopEditMode() {
    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_material);
    editText.setFocusable(false);
    editText.setFocusableInTouchMode(false);
    editButton.setVisibility(View.VISIBLE);
    if (isDirty()) saveFile();
    invalidateOptionsMenu();
  }


  private boolean isEditMode() {
    return editText.isFocusable();
  }


  private void toggleLineNumbers() {
    showingLineNumbers = !showingLineNumbers;
    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
    editor.putBoolean(KEY_SHOW_LINE_NUMBERS, showingLineNumbers);
    editor.commit();
    updateLineNumbers();
  }

  private void showMarkdownInsertionDialog() {
    class MdInsertionAction {
      final int descriptionRes;
      final Action0 actionFunc;
      public MdInsertionAction(int descriptionRes, Action0 actionFunc) {
        this.descriptionRes = descriptionRes;
        this.actionFunc = actionFunc;
      }
    }

    List<MdInsertionAction> actionItems = Lists.newArrayList(
        new MdInsertionAction(R.string.markdown_italic, () -> insertMdElement(R.string.markdown_element_italic)),
        new MdInsertionAction(R.string.markdown_bold, () -> insertMdElement(R.string.markdown_element_bold)),
        new MdInsertionAction(R.string.markdown_link, () -> insertMdElement(R.string.markdown_element_link)),
        new MdInsertionAction(R.string.markdown_image, () -> insertMdElement(R.string.markdown_element_image)),
        new MdInsertionAction(R.string.markdown_quote, () -> insertMdElement(R.string.markdown_element_quote)),
        new MdInsertionAction(R.string.markdown_code, () -> insertMdElement(R.string.markdown_element_code)),
        new MdInsertionAction(R.string.markdown_ul, () -> insertMdList(false)),
        new MdInsertionAction(R.string.markdown_ol, () -> insertMdList(true)),
        new MdInsertionAction(R.string.markdown_h1, () -> insertMdHeading(R.string.markdown_element_h1)),
        new MdInsertionAction(R.string.markdown_h2, () -> insertMdHeading(R.string.markdown_element_h2)),
        new MdInsertionAction(R.string.markdown_h3, () -> insertMdHeading(R.string.markdown_element_h3)),
        new MdInsertionAction(R.string.markdown_h4, () -> insertMdHeading(R.string.markdown_element_h4)),
        new MdInsertionAction(R.string.markdown_h5, () -> insertMdHeading(R.string.markdown_element_h5)),
        new MdInsertionAction(R.string.markdown_h6, () -> insertMdHeading(R.string.markdown_element_h6))
    );
    List<String> actionItemStrings = Lists.newArrayList();
    for (MdInsertionAction action : actionItems) {
      actionItemStrings.add(getString(action.descriptionRes));
    }

    new MaterialDialog.Builder(this)
        .title(R.string.action_insert_markdown)
        .items(actionItemStrings)
        .itemsCallback((dialog, itemView, position, text) -> {
          actionItems.get(position).actionFunc.call();
          inputMethodManager.showSoftInput(null, 0);
        })
        .negativeText(android.R.string.cancel)
        .show();
  }

  private void updateLineNumbers() {
    if (showingLineNumbers) {
      numLinesTextView.setVisibility(View.VISIBLE);
    } else {
      numLinesTextView.setVisibility(View.GONE);
      return;
    }

    // delay updating lines until internal layout has been built
    editText.post(() -> {
      numLinesTextView.setText("");
      int numLines = editText.getLineCount();
      int numCount = 1;
      for (int i = 0; i < numLines; ++i) {
        int start = editText.getLayout().getLineStart(i);
        if (start == 0) {
          numLinesTextView.append(numCount + "\n");
          numCount++;

        } else if (editText.getText().charAt(start - 1) == '\n') {
          numLinesTextView.append(numCount + "\n");
          numCount++;

        } else {
          numLinesTextView.append("\n");
        }
      }
      numLinesTextView.setTypeface(Typeface.MONOSPACE);
    });
  }


	private void insertMdElement(int separatorString) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		int offset = 0;

		String separator = getResources().getString(separatorString);
		String text = editText.getText().toString();
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();
		if(start == end) {
			switch(separatorString) {
				case R.string.markdown_element_bold:
				case R.string.markdown_element_italic:
					sb.append(separator);
					sb.append(separator);
					offset = sb.length()/2;
					break;
				case R.string.markdown_element_image:
					sb.append(separator);
					sb.append(" ");
					offset = 2;
					break;
				case R.string.markdown_element_link:
					sb.append(separator);
					sb.append(" ");
					offset = 1;
					break;
				case R.string.markdown_element_code:
				case R.string.markdown_element_quote:
					sb.append("\n");
                    sb.append(separator);
                    sb.append(" ");
					offset = sb.length();
					break;
				default:
					break;
			}
		} else {
			switch(separatorString) {
				case R.string.markdown_element_bold:
				case R.string.markdown_element_italic:
					sb.append(separator);
					sb.append(text.substring(start, end));
					sb.append(separator);
					offset = sb.length();
					break;
				case R.string.markdown_element_image:
					sb.append(separator);
					sb.append(" ");
					offset = 2;
					break;
				case R.string.markdown_element_link:
                    sb.append(separator);
                    sb.append(" ");
					offset = 1;
					break;
				case R.string.markdown_element_code:
				case R.string.markdown_element_quote:
                    sb.append("\n");
					for (String part : edited) {
						sb.append(separator);
						sb.append(" ");
						sb.append(part);
						sb.append("\n");
					}
					sb.append("\n");
					offset = sb.length();
					break;
				default:
					break;
			}
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + offset);
	}

	private void insertMdHeading(int headingString) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		String text = editText.getText().toString();
		String heading = getResources().getString(headingString);
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();

		if (start == end) {
			sb.append("\n");
			sb.append(heading);
			sb.append(" ");
		} else {
			sb.append("\n");
			sb.append(heading);
			for (String part : edited) {
				sb.append(" ");
				sb.append(part);
			}
			sb.append("\n\n");
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + sb.toString().length());
	}

	private void insertMdList(boolean ordered) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		String text = editText.getText().toString();
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		if(start == end) {
			if(ordered) {
				sb.append("1.");
				sb.append(" ");
			} else {
				String elem = getResources().getString(R.string.markdown_element_list);
				sb.append(elem);
				sb.append(" ");
			}
		} else {
			if (ordered) {
				for (int idx = 0; idx < edited.length; ++idx) {
					sb.append(String.valueOf(idx + 1));
					sb.append(". ");
					sb.append(edited[idx]);
					sb.append("\n");
				}
				sb.append("\n");
			} else {
				String elem = getResources().getString(R.string.markdown_element_list);
				for (String part : edited) {
					sb.append(elem);
					sb.append(" ");
					sb.append(part);
					sb.append("\n");
				}
				sb.append("\n");
			}
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + sb.toString().length());
	}

  /**
   * State of the {@link EditText}.
   */
  public static class EditTextState implements Serializable {

    public final String text;
    public final int cursorPosition;

    public EditTextState(String text, int cursorPosition) {
      this.text = text;
      this.cursorPosition = cursorPosition;
    }

  }

}

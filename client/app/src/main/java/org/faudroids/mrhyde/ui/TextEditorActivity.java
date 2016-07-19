package org.faudroids.mrhyde.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.eclipse.egit.github.core.Repository;
import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.DirNode;
import org.faudroids.mrhyde.git.FileData;
import org.faudroids.mrhyde.git.FileManager;
import org.faudroids.mrhyde.git.FileManagerFactory;
import org.faudroids.mrhyde.git.FileNode;
import org.faudroids.mrhyde.git.NodeUtils;
import org.faudroids.mrhyde.ui.utils.AbstractActionBarActivity;
import org.faudroids.mrhyde.ui.utils.UndoRedoEditText;
import org.faudroids.mrhyde.utils.DefaultErrorAction;
import org.faudroids.mrhyde.utils.DefaultTransformer;
import org.faudroids.mrhyde.utils.ErrorActionBuilder;
import org.faudroids.mrhyde.utils.HideSpinnerAction;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.Parcels;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

@ContentView(R.layout.activity_text_editor)
public final class TextEditorActivity extends AbstractActionBarActivity implements
		MarkdownFragment.MarkdownActionListener {

	private static final int EDITOR_MAX_HISTORY = 100;

	static final String
			EXTRA_REPOSITORY = "EXTRA_REPOSITORY",
			EXTRA_FILE_NODE = "EXTRA_FILE_NODE",
			EXTRA_IS_NEW_FILE = "EXTRA_IS_NEW_FILE";

	private static final String
			STATE_FILE_DATA = "STATE_FILE_DATA",
			STATE_EDIT_TEXT = "STATE_EDIT_TEXT",
			STATE_EDIT_MODE = "STATE_EDIT_MODE",
			STATE_UNDO_REDO = "STATE_UNDO_REDO";

	private static final int
			REQUEST_COMMIT = 42;

	private static final String
			PREFS_NAME = TextEditorActivity.class.getSimpleName();

	private static final String
			KEY_SHOW_LINE_NUMBERS = "KEY_SHOW_LINE_NUMBERS";

	@Inject private ActivityIntentFactory intentFactory;
	@Inject private FileManagerFactory fileManagerFactory;
	@Inject private InputMethodManager inputMethodManager;

	@InjectView(R.id.content) private EditText editText;
	private UndoRedoEditText undoRedoEditText;

	@InjectView(R.id.edit) private FloatingActionButton editButton;
	@InjectView(R.id.line_numbers) private TextView numLinesTextView;

	@Inject private NodeUtils nodeUtils;
	private Repository repository;
	private FileManager fileManager;
	private FileData fileData; // file currently being edited
	private boolean showingLineNumbers;

	private MarkdownFragment fragment;


	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// load arguments
		final boolean isNewFile = getIntent().getBooleanExtra(EXTRA_IS_NEW_FILE, false);
		repository = (Repository) getIntent().getSerializableExtra(EXTRA_REPOSITORY);
		fileManager = fileManagerFactory.createFileManager(repository);

		// hide line numbers by default
		showingLineNumbers = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_SHOW_LINE_NUMBERS, false);

		// start editing on long click
		editText.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (isEditMode()) return false;
				startEditMode();
				return true;
			}
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
		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startEditMode();
			}
		});

		// setup undo / redo
		undoRedoEditText = new UndoRedoEditText(editText);
		undoRedoEditText.setMaxHistorySize(EDITOR_MAX_HISTORY);

		// load selected file
		if (savedInstanceState != null && savedInstanceState.getSerializable(STATE_FILE_DATA) != null) {
			editText.post(new Runnable() {
				@Override
				public void run() {
					fileData = (FileData) savedInstanceState.getSerializable(STATE_FILE_DATA);
					boolean startEditMode = savedInstanceState.getBoolean(STATE_EDIT_MODE);
					EditTextState editTextState = Parcels.unwrap(savedInstanceState.getParcelable(STATE_EDIT_TEXT));
					showContent(startEditMode, editTextState);
					undoRedoEditText.restoreInstanceState(savedInstanceState, STATE_UNDO_REDO);
				}
			});

		} else {
			loadContent(isNewFile);
		}

	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATE_FILE_DATA, fileData);
		outState.putBoolean(STATE_EDIT_MODE, isEditMode());
		EditTextState editTextState = new EditTextState(editText.getText().toString(), editText.getSelectionStart());
		outState.putParcelable(STATE_EDIT_TEXT, Parcels.wrap(editTextState));
		undoRedoEditText.saveInstanceState(outState, STATE_UNDO_REDO);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.text_editor, menu);
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
		}

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Timber.d("back pressed");
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

			case R.id.action_commit:
				saveFile();
				startActivityForResult(intentFactory.createCommitIntent(repository), REQUEST_COMMIT);
				return true;

			case R.id.action_preview:
				saveFile();
				startActivity(intentFactory.createPreviewIntent(repository));
				return true;

			case R.id.action_insert_markdown:
				showOverlay();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_COMMIT:
				if (resultCode != RESULT_OK) return;
				if (isEditMode()) stopEditMode();
				loadContent(false);
		}
	}


	@Override
	public void onBackPressed() {
		if (isEditMode()) {
			if (!isDirty()) {
				returnResult();

			} else {
				new AlertDialog.Builder(this)
						.setTitle(R.string.save_title)
						.setMessage(R.string.save_message)
						.setCancelable(false)
						.setPositiveButton(getString(R.string.save_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								saveFile();
								returnResult();
							}
						})
						.setNegativeButton(getString(R.string.save_cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								returnResult();
							}
						})
						.show();
			}
		} else {
			returnResult();
		}
	}


	private void loadContent(final boolean isNewFile) {
		showSpinner();
		compositeSubscription.add(fileManager.getTree()
				.flatMap(new Func1<DirNode, Observable<FileData>>() {
					@Override
					public Observable<FileData> call(DirNode rootNode) {
						FileNode node = (FileNode) nodeUtils.restoreNode(EXTRA_FILE_NODE, getIntent(), rootNode);

						if (!isNewFile) {
							return fileManager.readFile(node);
						} else {
							return Observable.just(new FileData(node, new byte[0]));
						}
					}
				})
				.compose(new DefaultTransformer<FileData>())
				.subscribe(new Action1<FileData>() {
					@Override
					public void call(FileData file) {
						hideSpinner();
						TextEditorActivity.this.fileData = file;
						showContent(isNewFile, null);
					}
				}, new ErrorActionBuilder()
						.add(new DefaultErrorAction(this, "failed to get file content"))
						.add(new HideSpinnerAction(this))
						.build()));
	}


	private void showContent(boolean startEditMode, @Nullable EditTextState editTextState) {
		setTitle(fileData.getFileNode().getPath());
		try {
			// set text
			if (editTextState != null) editText.setText(editTextState.text);
			else editText.setText(new String(fileData.getData(), "UTF-8"));
			undoRedoEditText.clearHistory(); // otherwise setting this text would be part of history
			editText.setTypeface(Typeface.MONOSPACE);

			// start edit mode
			if (startEditMode) startEditMode();
			else stopEditMode();

			// restore cursor position
			if (editTextState != null) {
				editText.setSelection(editTextState.cursorPosition);
			}

		} catch (UnsupportedEncodingException uee) {
			Timber.e(uee, "failed to read content");
		}
		updateLineNumbers();
	}


	private void saveFile() {
		Timber.d("saving file");
		try {
			fileManager.writeFile(new FileData(fileData.getFileNode(), editText.getText().toString().getBytes()));
		} catch (IOException ioe) {
			Timber.e(ioe, "failed to write file");
			// TODO
		}
	}


	/**
	 * @return true if the file has been changed
	 */
	private boolean isDirty() {
		if (fileData == null) return false;
		try {
			return !new String(fileData.getData(), "UTF-8").equals(editText.getText().toString());
		} catch (UnsupportedEncodingException uee) {
			Timber.e(uee, "failed to encoding content");
			return false;
		}
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
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
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


	private void showOverlay() {
		MarkdownFragment mdf = new MarkdownFragment();
		mdf.show(getFragmentManager(), "MarkdownFragment");
	}


	private void updateLineNumbers() {
		if (showingLineNumbers) {
			numLinesTextView.setVisibility(View.VISIBLE);
		} else {
			numLinesTextView.setVisibility(View.GONE);
			return;
		}

		// delay updating lines until internal layout has been built
		editText.post(new Runnable() {
			@Override
			public void run() {
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
			}
		});
	}

	@Override
	public void onInsertBoldText(DialogFragment dialog) {
		int pos = editText.getSelectionStart();
		String text = editText.getText().toString();
		String elem = getResources().getString(R.string.markdown_element_bold);
		StringBuilder sb = new StringBuilder();
		text = text.substring(0, pos) + elem + text.substring(pos, text.length());
		editText.setText(text, TextView.BufferType.EDITABLE);
		editText.setSelection(pos + elem.length()/2);
	}

	@Override
	public void onInsertItalicText(DialogFragment dialog) {
		int pos = editText.getSelectionStart();
		String text = editText.getText().toString();
		String elem = getResources().getString(R.string.markdown_element_italic);
		text = text.substring(0, pos) + elem + text.substring(pos, text.length());
		editText.setText(text, TextView.BufferType.EDITABLE);
		editText.setSelection(pos + elem.length()/2);
	}

	@Override
	public void onInsertLink(DialogFragment dialog) {
		int pos = editText.getSelectionStart();
		String text = editText.getText().toString();
		String elem = getResources().getString(R.string.markdown_element_link);
		text = text.substring(0, pos) + elem + text.substring(pos, text.length());
		editText.setText(text, TextView.BufferType.EDITABLE);
		editText.setSelection(pos + 1);
	}

	@Override
	public void onInsertQuote(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_quote);
		this.insertElement(elem);
	}

	@Override
	public void onInsertCode(DialogFragment dialog) {
		this.insertElement("    ");
	}

	@Override
	public void onInsertUnorderedList(DialogFragment dialog) {
		this.createList(false);
	}

	@Override
	public void onInsertOrderedList(DialogFragment dialog) {
		this.createList(true);
	}

	@Override
	public void onInsertH1(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h1);
		this.makeHeading(elem);
	}

	@Override
	public void onInsertH2(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h2);
		this.makeHeading(elem);
	}

	@Override
	public void onInsertH3(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h3);
		this.makeHeading(elem);
	}

	@Override
	public void onInsertH4(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h4);
		this.makeHeading(elem);
	}

	@Override
	public void onInsertH5(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h5);
		this.makeHeading(elem);
	}

	@Override
	public void onInsertH6(DialogFragment dialog) {
		String elem = getResources().getString(R.string.markdown_element_h6);
		this.makeHeading(elem);
	}

	private void insertElement(String separator) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		String text = editText.getText().toString();
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();
		if(start == end) {
			sb.append(separator);
			sb.append(" ");
		} else {
			for (String part : edited) {
				sb.append(separator);
				sb.append(" ");
				sb.append(part);
				sb.append("\n");
			}
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + sb.toString().length());
	}

	private void makeHeading(String heading) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		String text = editText.getText().toString();
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();
		if (start == end) {
			sb.append(heading);
			sb.append(" ");
		} else {
			sb.append(heading);
			for (String part : edited) {
				sb.append(" ");
				sb.append(part);
			}
			sb.append("\n");
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + sb.toString().length());
	}

	private void createList(boolean ordered) {
		int start = editText.getSelectionStart();
		int end = editText.getSelectionEnd();
		String text = editText.getText().toString();
		String[] edited = text.substring(start, end).split("\n");
		StringBuilder sb = new StringBuilder();

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
			} else {
				String elem = getResources().getString(R.string.markdown_element_list);
				for (String part : edited) {
					sb.append(elem);
					sb.append(" ");
					sb.append(part);
					sb.append("\n");
				}
			}
		}

		String result = text.substring(0, start) + sb.toString() + text.substring(end, text
				.length());

		editText.setText(result, EditText.BufferType.EDITABLE);
		editText.setSelection(start + sb.toString().length());
	}

	/**
	 * State of the {@link EditText}. Public because required by Parceler.
	 */
	@Parcel
	public static class EditTextState {

		public final String text;
		public final int cursorPosition;

		@ParcelConstructor
		public EditTextState(String text, int cursorPosition) {
			this.text = text;
			this.cursorPosition = cursorPosition;
		}

	}

}

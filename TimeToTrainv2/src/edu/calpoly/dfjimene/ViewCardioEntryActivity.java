package edu.calpoly.dfjimene;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewCardioEntryActivity extends SherlockFragmentActivity {

	/** Session ID for this entry */
	private long m_sessionId;

	/** Entry this activity pertains to */
	private ExerciseEntry m_entry;

	/** Entry ID for this entry */
	private long m_entryId;

	/** Entry Uri string */
	private static final String ENTRY_CONTENT_STRING = TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES;

	/** TextView holder for when there is no note to display */
	TextView m_noteView;

	/** Layout for entire thing */
	LinearLayout m_mainLayout;
	
	/** Note layout container */
	LinearLayout m_noteLayout = null;
	LinearLayout m_editNoteLayout = null;

	/** Edit Text for editing notes */
	EditText m_editor = null;

	/** Add Note Buttons */
	Button m_addNoteButton;
	Button m_submitButton;

	/** On click listeners for submitting a note */
	private OnClickListener m_addNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			buildNoteEditor();

		}
	};

	private OnClickListener m_submitNewNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			submitEditedNote();
		}
	};
	
	private OnClickListener m_editNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			editNote();
		}
	};

	/** Cardio Entry projection */
	public static final String PROJECTION[] = {
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_TIME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_DISTANCE,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		m_sessionId = extras.getLong("session");
		m_entryId = extras.getLong("entry");
		buildCardioExerciseEntryFromId();
		initCardioView();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void buildCardioExerciseEntryFromId() {
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null,
				null);
		if (cursor.getCount() != 1) {
			Log.e(ViewCardioEntryActivity.class.getName(),
					"Rows returned should be 1! The ids are unique!");
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			cursor.close();
			return;
		}
		cursor.moveToFirst();
		m_entry = new ExerciseEntry(m_entryId, m_sessionId,
				cursor.getString(0), ExerciseEntry.TYPE_CARDIO);
		if (!cursor.isNull(3))
			m_entry.setComments(cursor.getString(3));
		else
			m_entry.setComments(null);
		if (!cursor.isNull(2))
			m_entry.setDistance(cursor.getDouble(2));
		else
			m_entry.setDistance(-1);
		if (!cursor.isNull(1))
			m_entry.setTime(cursor.getInt(1));
		else
			m_entry.setTime(-1);
		cursor.close();
	}

	public void initCardioView() {
		setTitle(m_entry.getExreciseName());
		setContentView(R.layout.view_entry_cardio);
		TextView time = (TextView) findViewById(R.id.cardio_time_view);
		TextView dist = (TextView) findViewById(R.id.cardio_dist_view);
		LinearLayout layout = (LinearLayout) time.getParent();
		if (m_entry.getTime() != -1) {
			int min = m_entry.getTime() / 60;
			int sec = m_entry.getTime() % 60;
			time.setText(min + ":" + (sec >= 10 ? sec : ("0" + sec)));
		} else {
			layout.removeView(time);
			layout.removeView(findViewById(R.id.your_time));
		}
		if (m_entry.getDistance() != -1) {
			dist.setText(m_entry.getDistance() + "mi");
		} else {
			layout.removeView(dist);
			layout.removeView(findViewById(R.id.your_distance));
		}
		if (m_entry.getComments() != null) {
			m_noteView = (TextView) findViewById(R.id.cardio_note_view);
			m_noteLayout = (LinearLayout) m_noteView.getParent();
			m_noteView.setText(m_entry.getComments());
			m_addNoteButton = (Button) findViewById(R.id.add_cardio_note);
			m_addNoteButton.setText("Edit Note");
			m_addNoteButton.setOnClickListener(m_editNoteListener);
		} else {
			m_noteView = (TextView) findViewById(R.id.cardio_note_view);
			m_noteLayout = (LinearLayout) m_noteView.getParent();
			m_noteLayout.removeView(m_noteView);
			m_addNoteButton = (Button) findViewById(R.id.add_cardio_note);
			m_addNoteButton.setOnClickListener(m_addNoteListener);
		}
		m_mainLayout = (LinearLayout) m_noteLayout.getParent();
	}

	public void buildNoteEditor() {
		if (m_noteLayout == null) {
			Log.e(ViewCardioEntryActivity.class.getName(),
					"This layout should never be null if the "
							+ "button to add notes is present");
		}

		if (m_editNoteLayout == null) {
			LayoutInflater inflater = getLayoutInflater();
			m_editNoteLayout = (LinearLayout) inflater.inflate(
					R.layout.add_note, null);
			m_editor = (EditText) m_editNoteLayout.findViewById(R.id.edit_note);
			m_submitButton = (Button) m_editNoteLayout
					.findViewById(R.id.submit_button);
			m_editor.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
						submitEditedNote();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP
							&& keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					return false;
				}
			});
			m_submitButton.setOnClickListener(m_submitNewNoteListener);
		}
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.requestFocus();

	}

	public void submitEditedNote() {
		m_noteLayout.removeView(m_editNoteLayout);
		m_addNoteButton.setText("Edit Note");
		m_addNoteButton.setOnClickListener(m_editNoteListener);
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT, m_editor
				.getText().toString());
		m_entry.setComments(m_editor.getText().toString());
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		getContentResolver().update(uri, values, null, null);
		m_noteView.setText(m_entry.getComments());
		m_noteLayout.addView(m_noteView);
		m_addNoteButton.setText("Edit Note");
		m_addNoteButton.setOnClickListener(m_editNoteListener);
		m_mainLayout.addView(m_addNoteButton);

	}
	
	public void editNote(){
		if (m_noteLayout == null) {
			Log.e(ViewCardioEntryActivity.class.getName(),
					"This layout should never be null if the "
							+ "button to edit notes is present");
		}
		if (m_editNoteLayout == null) {
			LayoutInflater inflater = getLayoutInflater();
			m_editNoteLayout = (LinearLayout) inflater.inflate(
					R.layout.add_note, null);
			m_editor = (EditText) m_editNoteLayout.findViewById(R.id.edit_note);
			m_submitButton = (Button) m_editNoteLayout
					.findViewById(R.id.submit_button);
			m_editor.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
						submitEditedNote();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP
							&& keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					return false;
				}
			});
			m_submitButton.setOnClickListener(m_submitNewNoteListener);
		}
		m_noteLayout.removeView(m_noteView);
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.setText(m_entry.getComments());
		m_editor.requestFocus();
	}
}

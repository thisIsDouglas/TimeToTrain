package edu.calpoly.dfjimene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class ViewStrengthEntryActivity extends SherlockFragmentActivity {

	/** Json keys */
	private static final String JSON_ARR_KEY = "sets";
	private static final String JSON_WEIGHT_KEY = "weight";
	private static final String JSON_WEIGHT_UNITS = "units";
	private static final String JSON_REPS = "reps";

	/** Checked indicators */
	private static final int KG_CHECKED = 0;
	private static final int LB_CHECKED = 1;

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

	/** Set layout container */
	LinearLayout m_setLayout;
	LinearLayout m_editSetLayout;

	/** ArrayList containing sets */
	ArrayList<Map<String, String>> m_listSets = new ArrayList<Map<String, String>>();

	/** Note layout container */
	LinearLayout m_noteLayout = null;
	LinearLayout m_editNoteLayout = null;

	/** Edit Text for editing notes */
	EditText m_editor = null;

	/** Add Note Buttons */
	Button m_addNoteButton;
	Button m_submitButton;

	/** Add Set Buttons */
	Button m_addSetButton;
	Button m_submitSet;
	RadioGroup m_editSetGroup;

	/** Selected units */
	private int m_checked = KG_CHECKED;

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

	/** Listeners for adding a set */
	private OnClickListener m_addSetListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			buildSetEditor();

		}
	};

	private OnClickListener m_submitSetListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			submitNewSet();
		}
	};

	/** Projection for strength entry attributes */
	public static final String PROJECTION[] = {
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		m_sessionId = extras.getLong("session");
		m_entryId = extras.getLong("entry");
		buildStrengthExerciseEntryFromId();
		initStrengthView();
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

	public void buildStrengthExerciseEntryFromId() {
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null,
				null);
		if (cursor.getCount() != 1) {
			Log.e(ViewStrengthEntryActivity.class.getName(),
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
				cursor.getString(0), ExerciseEntry.TYPE_STRENGTH);
		if (!cursor.isNull(2))
			m_entry.setComments(cursor.getString(2));
		else
			m_entry.setComments(null);
		if (!cursor.isNull(1))
			m_entry.setSets(cursor.getString(1));
		else
			m_entry.setSets(new JSONObject().toString());
		cursor.close();
	}

	public void initStrengthView() {
		setTitle(m_entry.getExreciseName());
		setContentView(R.layout.view_entry_strength);
		m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
		m_setLayout = (LinearLayout) findViewById(R.id.add_strength_set_container);
		m_mainLayout = (LinearLayout) m_noteLayout.getParent();
		m_addSetButton = (Button) findViewById(R.id.add_strength_set);
		m_setLayout.removeAllViews();
		if (m_entry.getComments() != null) {
			m_noteView = (TextView) findViewById(R.id.strength_note_view);
			m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
			m_noteView.setText(m_entry.getComments());
			m_addNoteButton = (Button) findViewById(R.id.add_strength_note);
			m_addNoteButton.setText("Edit Note");
			m_addNoteButton.setOnClickListener(m_editNoteListener);
		} else {
			m_noteView = (TextView) findViewById(R.id.strength_note_view);
			m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
			m_noteLayout.removeView(m_noteView);
			m_addNoteButton = (Button) findViewById(R.id.add_strength_note);
			m_addNoteButton.setOnClickListener(m_addNoteListener);
		}
		JSONObject json;
		JSONArray jsonArr;
		// Do all json work inside this try/catch statement
		try {
			json = new JSONObject(m_entry.getSets());

			if (json.has(JSON_ARR_KEY)) {
				jsonArr = json.getJSONArray(JSON_ARR_KEY);

				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject arrObj;
					arrObj = jsonArr.getJSONObject(i);
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(JSON_REPS, arrObj.getString(JSON_REPS));
					map.put(JSON_WEIGHT_KEY, arrObj.getString(JSON_WEIGHT_KEY));
					map.put(JSON_WEIGHT_UNITS,
							arrObj.getString(JSON_WEIGHT_UNITS));
					m_listSets.add(map);
				}
			}
		} catch (JSONException e) {
			Log.e(ViewStrengthEntryActivity.class.getName(), "Bad JSON format.");
			e.printStackTrace();
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return;
		}
		LayoutInflater inflater = getLayoutInflater();
		for (int i = 0; i < m_listSets.size(); i++) {
			Map<String, String> m = m_listSets.get(i);
			LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.set_view, null);

			TextView view = (TextView) layout.findViewById(R.id.set_num_title);
			view.setText("Set " + (i + 1));

			view = (TextView) layout.findViewById(R.id.weight_title);
			view.setText(m.get(JSON_WEIGHT_KEY));

			view = (TextView) layout.findViewById(R.id.units_for_set);
			view.setText(m.get(JSON_WEIGHT_UNITS));

			view = (TextView) layout.findViewById(R.id.reps_for_set);
			view.setText(m.get(JSON_REPS));

			m_setLayout.addView(layout);
		}
		m_setLayout.addView(m_addSetButton);
		m_addSetButton.setOnClickListener(m_addSetListener);
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

	public void editNote() {
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

	public void buildSetEditor() {
		if (m_setLayout == null) {
			Log.e(ViewCardioEntryActivity.class.getName(),
					"This layout should never be null if the "
							+ "button to add notes is present");
		}

		if (m_editSetLayout == null) {
			LayoutInflater inflater = getLayoutInflater();
			m_editSetLayout = (LinearLayout) inflater.inflate(R.layout.set_add,
					null);
			m_submitSet = (Button) m_editSetLayout
					.findViewById(R.id.confirm_set);
			m_submitSet.setOnClickListener(m_submitSetListener);
			m_editSetGroup = (RadioGroup) m_editSetLayout.findViewById(R.id.units_group);
			m_editSetGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							switch (checkedId) {
							case R.id.kg_button:
								m_checked = KG_CHECKED;
								return;
							case R.id.lb_button:
								m_checked = LB_CHECKED;
								return;
							}
						}
					});
			m_editSetGroup.check(R.id.kg_button);
		}
		m_setLayout.removeView(m_addSetButton);
		m_setLayout.addView(m_editSetLayout);

	}

	public void submitNewSet() {
		EditText reps, weight;
		reps = (EditText) m_setLayout.findViewById(R.id.edit_reps);
		weight = (EditText) m_setLayout.findViewById(R.id.edit_weight);

		if (reps.getText().toString().equals("")
				|| weight.getText().toString().equals("")) {
			reps.setText("");
			weight.setText("");
			m_setLayout.removeView(m_editSetLayout);
			m_setLayout.addView(m_addSetButton);
			return;
		}

		HashMap<String, String> map = new HashMap<String, String>();
		if (m_checked == KG_CHECKED)
			map.put(JSON_WEIGHT_UNITS, "kg");
		else
			map.put(JSON_WEIGHT_UNITS, "lb");
		map.put(JSON_WEIGHT_KEY, weight.getText().toString());
		map.put(JSON_REPS, reps.getText().toString());
		weight.setText("");
		reps.setText("");
		m_setLayout.removeView(m_editSetLayout);
		addSetToList(map);
		m_setLayout.addView(m_addSetButton);
	}

	public void addSetToList(Map<String, String> map) {
		LayoutInflater inflater = getLayoutInflater();
		m_listSets.add(map);
		LinearLayout setLayout = (LinearLayout) inflater.inflate(
				R.layout.set_view, null);
		TextView view = (TextView) setLayout.findViewById(R.id.set_num_title);
		view.setText("Set " + m_listSets.size());

		view = (TextView) setLayout.findViewById(R.id.weight_title);
		view.setText(map.get(JSON_WEIGHT_KEY));

		view = (TextView) setLayout.findViewById(R.id.units_for_set);
		view.setText(map.get(JSON_WEIGHT_UNITS));

		view = (TextView) setLayout.findViewById(R.id.reps_for_set);
		view.setText(map.get(JSON_REPS));

		m_setLayout.addView(setLayout);
		syncSetListWithDb();

	}

	public void syncSetListWithDb() {
		JSONObject json;
		// Do all json work inside this try/catch statement
		try {
			JSONArray jsonArr = new JSONArray();
			for (Map<String, String> m : m_listSets) {
				JSONObject arrObj = new JSONObject();
				
				arrObj.put(JSON_WEIGHT_KEY, m.get(JSON_WEIGHT_KEY));
				arrObj.put(JSON_REPS, m.get(JSON_REPS));
				arrObj.put(JSON_WEIGHT_UNITS, m.get(JSON_WEIGHT_UNITS));
				jsonArr.put(arrObj);
			}
			json = new JSONObject();
			json.put(JSON_ARR_KEY, jsonArr);

		} catch (JSONException e) {
			Log.e(ViewStrengthEntryActivity.class.getName(), "Bad JSON format.");
			e.printStackTrace();
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return;
		}
		m_entry.setSets(json.toString());
		
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS, m_entry.getSets());
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		getContentResolver().update(uri, values, null, null);
	}
}

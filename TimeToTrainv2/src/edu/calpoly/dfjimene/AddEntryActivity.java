package edu.calpoly.dfjimene;

import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.Spanned;
// import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * Add Entry Activity class that users navigate to when they are adding a new
 * entry. The activity manages all user interaction for adding entries.
 * 
 * @author Douglas Jimenez
 * 
 * 
 */
public class AddEntryActivity extends SherlockFragmentActivity implements
		OnCheckedChangeListener {

	/** Constants for altering the view */
	private static final int STRENGTH = 0;
	private static final int CARDIO = 1;
	private static final int UNCHECKED = -1;

	/** Session ID of session the entry will be added to */
	private long m_sessionId;

	/** Radio group for the add entry activity */
	private RadioGroup m_radioGroup;

	/** Edit text for the exercise name */
	private EditText m_exerciseName;

	/** Edit text for distance */
	private EditText m_distance;

	/** Edit text for seconds field */
	private EditText m_timeSeconds;

	/** Edit text for minutes field */
	private EditText m_timeMinutes;

	/** Layout for extra cardio portion */
	private LinearLayout m_cardioLayout;

	/** Strength tip view */
	private TextView m_strTip;

	/** Currently selected type */
	private int m_selected = STRENGTH;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Always call parent's onCreate
		super.onCreate(savedInstanceState);

		// Get extras where the session ID is stored and stores it in member
		// variable
		Bundle extras = getIntent().getExtras();
		m_sessionId = extras.getLong(SessionDetailsActivity.SESSION_ID);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		initLayout();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu to the action bar
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.add_entry_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Filter menu items for home button or next being clicked
		switch (item.getItemId()) {
		case R.id.menu_next:
			// If next is selected, call addNewEntry and return
			addNewEntry();
			return true;
		case android.R.id.home:
			// If home is selected, place the session ID back in extras and
			// return to the parent activity and end this one
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Initializes the layout for adding new entries to the database. The
	 * suppressions are for lines that are already checked before being
	 * executed.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void initLayout() {

		// Set the view to the add entry view and set the title appropriately
		setContentView(R.layout.add_entry_view);
		setTitle("Add New Entry");

		// Inflate the extra cardio information into a readily available member
		// variable and initialize the second, minute and distance member
		// EditTexts to the ones inflated by the inflater
		LayoutInflater inflater = getLayoutInflater();
		m_cardioLayout = (LinearLayout) inflater.inflate(
				R.layout.add_entry_cardio_append, null);
		m_distance = (EditText) m_cardioLayout.findViewById(R.id.edit_distance);
		m_timeMinutes = (EditText) m_cardioLayout
				.findViewById(R.id.edit_minutes);
		m_timeSeconds = (EditText) m_cardioLayout
				.findViewById(R.id.edit_seconds);
		// Set input filter for seconds so that the range of numbers it can
		// accept is 0-59
		m_timeSeconds.setFilters(new InputFilter[] { new InputFilterMinMax("0",
				"59") });

		// Initialize the radio group member variable with inflated radio group
		// and EditText for exercise name
		m_radioGroup = (RadioGroup) findViewById(R.id.group_exercise_type);
		m_radioGroup.check(R.id.radio_strength);
		m_exerciseName = (EditText) findViewById(R.id.edit_entry_name);
		m_strTip = (TextView) findViewById(R.id.strength_tip);
		m_radioGroup.setOnCheckedChangeListener(this);

		// Set the background image to the TTT logo
		LinearLayout layout = (LinearLayout) m_exerciseName.getParent();
		Resources res = getResources();
		Drawable d = res.getDrawable(R.drawable.background_icon);
		d.setAlpha(45);
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			layout.setBackgroundDrawable(d);
		} else {
			layout.setBackground(d);
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// Figure out which button is checked and update the view
		switch (checkedId) {
		case R.id.radio_strength:
			alterLayout(STRENGTH);
			m_selected = STRENGTH;
			break;
		case R.id.radio_cardio:
			alterLayout(CARDIO);
			m_selected = CARDIO;
			break;
		case -1:
			alterLayout(UNCHECKED);
			m_selected = UNCHECKED;
			break;
		}

	}

	/**
	 * Checks if the required fields for an exercise type are filled and then
	 * adds the entry. After the entry has been added, the user is sent to the
	 * ViewEntryActivity specific to the exercise type
	 */
	private void addNewEntry() {

		// Initialize ContentValues to place column values into and check for
		// the presence of an exercise name
		ContentValues values = new ContentValues();
		if (m_exerciseName.getText().toString().equals("")) {
			Toast.makeText(this, "Please enter an exercise name",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// If cardio is selected, check cardio fields for input. If strength is
		// selected, go on to the view strength entry activity
		if (m_selected == CARDIO) {

			// Parse the minute, second and distance EditTexts for their values,
			// then check if the required fields are filled. At least either a
			// time or a distance must be present to submit a cardio entry
			Integer min, sec;

			try {
				min = Integer.parseInt(m_timeMinutes.getText().toString());
			} catch (Exception e) {
				min = null;
			}
			try {
				sec = Integer.parseInt(m_timeSeconds.getText().toString());
			} catch (Exception e) {
				sec = null;
			}
			Double dist;
			try {
				dist = Double.parseDouble(m_distance.getText().toString());
			} catch (NumberFormatException e) {
				dist = null;
			}
			int time;

			// Return if none are filled
			if (min == null && sec == null && dist == null)
				return;
			else {

				// The required values are filled! Add them to the ContentValues
				// and then insert the values to the DB
				values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_DISTANCE,
						dist);
				time = (min == null ? 0 : min.intValue()) * 60
						+ (sec == null ? 0 : sec.intValue());
				values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TIME,
						(time == 0 ? null : time));
				values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE,
						ExerciseEntry.TYPE_CARDIO);
				values.put(
						TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
						m_exerciseName.getText().toString());
				values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
						m_sessionId);
				Uri uri = Uri
						.parse(TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
								+ "/entry/0");

				// Get the returned URI and grab the entry ID pulled at the end
				// of it
				uri = getContentResolver().insert(uri, values);
				String str = uri.getLastPathSegment();
				if (str == null) {

					// We didn't get an entry ID. Something went wrong. Return
					// to parent activity
					Intent i = new Intent(this, SessionDetailsActivity.class);
					i.putExtra(SessionListActivity.INTENT_SESSION_ID,
							m_sessionId);
					NavUtils.navigateUpTo(this, i);
					finish();
					return;
				}

				// Parse entry ID
				long entryId;
				try {
					entryId = Long.parseLong(str);
				} catch (NumberFormatException e) {
					// Something went wrong parsing the ID. Return
					// to the parent activity
					Intent i = new Intent(this, SessionDetailsActivity.class);
					i.putExtra(SessionListActivity.INTENT_SESSION_ID,
							m_sessionId);
					NavUtils.navigateUpTo(this, i);
					finish();
					return;
				}

				// Create a new intent and add the session ID and Entry ID to
				// its extras. Then launch the activity
				Intent i = new Intent(this, ViewCardioEntryActivity.class);
				i.putExtra("session", m_sessionId);
				i.putExtra("entry", entryId);
				startActivity(i);
				m_distance.setText("");
				m_timeMinutes.setText("");
				m_timeSeconds.setText("");
				m_exerciseName.setText("");
				finish();
			}
		} else {

			// Initialize values for the strength set
			values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
					new JSONObject().toString());
			values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE,
					ExerciseEntry.TYPE_STRENGTH);
			values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
					m_exerciseName.getText().toString());
			values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
					m_sessionId);

			Uri uri = Uri
					.parse(TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
							+ "/entry/0");

			// Insert the new strength entry in the DB and grab the resulting
			// URI
			uri = getContentResolver().insert(uri, values);
			String str = uri.getLastPathSegment();
			if (str == null) {

				// Entry ID is null. Something went wrong. Return to parent
				// activity
				Intent i = new Intent(this, SessionDetailsActivity.class);
				i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
				NavUtils.navigateUpTo(this, i);
				finish();
				return;
			}
			long entryId;

			// Attempt to parse the ID
			try {
				entryId = Long.parseLong(str);
			} catch (NumberFormatException e) {

				// Something went wrong parsing the ID. Return to parent
				// activity
				Intent i = new Intent(this, SessionDetailsActivity.class);
				i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
				NavUtils.navigateUpTo(this, i);
				finish();
				return;
			}

			// New entry has been inserted. Set add session and entry IDs to
			// extras and start the view strength activity
			Intent i = new Intent(this, ViewStrengthEntryActivity.class);
			i.putExtra("session", m_sessionId);
			i.putExtra("entry", entryId);
			startActivity(i);
			m_distance.setText("");
			m_timeMinutes.setText("");
			m_timeSeconds.setText("");
			m_exerciseName.setText("");
			finish();
		}
	}

	/**
	 * Alters the extra layout space based on the exercise type
	 * 
	 * @param type
	 *            the exercise type
	 */
	private void alterLayout(int type) {
		LinearLayout layout;

		// If strength, append strength message. If cardio, append the cardio
		// EditTexts
		if (m_selected == CARDIO && (type == STRENGTH || type == UNCHECKED)) {
			layout = ((LinearLayout) m_cardioLayout.getParent());
			layout.removeView(m_cardioLayout);
			layout.addView(m_strTip);
		} else if (m_selected == STRENGTH && type == CARDIO) {

			layout = ((LinearLayout) this.findViewById(R.id.add_entry_linear));
			layout.removeView(m_strTip);
			layout.addView(m_cardioLayout);
		}
	}

	/**
	 * MinMax input filter for accepting a range of values in an EditText
	 * 
	 */
	private class InputFilterMinMax implements InputFilter {

		/** Minimum and maximum member variables */
		private int min, max;

		/**
		 * Constructor for the input filter. Takes 2 strings used as min and max
		 * values the EditText can have
		 * 
		 * @param min
		 *            minimum string
		 * @param max
		 *            maximum string
		 */
		public InputFilterMinMax(String min, String max) {
			this.min = Integer.parseInt(min);
			this.max = Integer.parseInt(max);
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			try {

				// Parse input appended to the already existing string, then
				// check if the number is in the set range. Throw an
				// exception if the resulting string is not a number
				int input = Integer.parseInt(dest.toString()
						+ source.toString());
				if (isInRange(min, max, input))
					return null;
			} catch (NumberFormatException nfe) {
			}
			return "";
		}

		/**
		 * Checks if c is within the range set by a and b
		 * @param a the min value
		 * @param b the max value
		 * @param c the value to test
		 * @return true if within range, false otherwise
		 */
		private boolean isInRange(int a, int b, int c) {
			return b > a ? c >= a && c <= b : c >= b && c <= a;
		}
	}

}

package edu.calpoly.dfjimene.exerciseentry;

import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryView.OnSimpleEntryChangeListener;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class is a cursor adapter for simple entries. It takes entries from a
 * source and inserts them into their respective views. It then produces these
 * views for a list view to display
 * 
 * @author Douglas Jimenez
 * 
 */
public class SimpleEntryCursorAdapter extends CursorAdapter {

	/** Listener for when a SimpleEntry View changes */
	private OnSimpleEntryChangeListener m_listener;

	/**
	 * Constructor that takes in the application Context in which it is being
	 * used and the Collection of SimpleEntry objects to which it is bound.
	 * 
	 * @param context
	 *            The application Context.
	 * 
	 * @param sessionCursor
	 *            A Database Cursor containing SimpleEntries
	 * 
	 * @param flags
	 *            A list of flags that decide this adapter's behavior.
	 */
	public SimpleEntryCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	/**
	 * Mutator method for OnSessionChangeListener.
	 * 
	 * @param mListener
	 *            The OnSessionChangeListener that will be notified when a
	 *            session changes.
	 */
	public void setOnSimpleEntryChangeListener(
			OnSimpleEntryChangeListener mListener) {
		this.m_listener = mListener;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {

		// Create a new simple entry from the cursor and insert it into an
		// existing SimpleEntryView
		SimpleEntry entry;
		entry = new SimpleEntry(
				arg2.getLong(TimeToTrainTables.SIMPLE_ENTRIES_COL_SESSION_ID),
				arg2.getString(TimeToTrainTables.SIMPLE_ENTRIES_COL_EXERCISE_NAME),
				arg2.getLong(TimeToTrainTables.SIMPLE_ENTRIES_COL_ID), arg2
						.getInt(TimeToTrainTables.SIMPLE_ENTRIES_COL_TYPE));
		SimpleEntryView view = (SimpleEntryView) arg0;
		view.setOnSimpleEntryChangeListener(null);
		view.setSimpleEntry(entry);

		// Set the OnSimpleEntryChangeListener
		view.setOnSimpleEntryChangeListener(this.m_listener);

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {

		// Create a new simple entry from the cursor and insert it into a new
		// SimpleEntryView
		SimpleEntry entry;
		entry = new SimpleEntry(
				arg1.getLong(TimeToTrainTables.SIMPLE_ENTRIES_COL_SESSION_ID),
				arg1.getString(TimeToTrainTables.SIMPLE_ENTRIES_COL_EXERCISE_NAME),
				arg1.getLong(TimeToTrainTables.SIMPLE_ENTRIES_COL_ID), arg1
						.getInt(TimeToTrainTables.SIMPLE_ENTRIES_COL_TYPE));
		SimpleEntryView view = new SimpleEntryView(arg0, entry);

		// Set the OnSimpleEntryChangeListener and return the view
		view.setOnSimpleEntryChangeListener(this.m_listener);
		return view;
	}

}

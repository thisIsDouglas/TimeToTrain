package edu.calpoly.dfjimene.session;

import java.text.ParseException;

import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.session.SessionView.OnSessionChangeListener;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * SessionCursorAdapter class that is responsible for binding the data to a view
 * and posting it to the ListView.
 * 
 * @author Douglas Jimenez
 * 
 */
public class SessionCursorAdapter extends CursorAdapter {

	/** Listener for when a Session View changes */
	private OnSessionChangeListener m_listener;

	/**
	 * Constructor that takes in the application Context in which it is being
	 * used and the Collection of Session objects to which it is bound.
	 * 
	 * @param context
	 *            The application Context.
	 * 
	 * @param sessionCursor
	 *            A Database Cursor containing sessions
	 * 
	 * @param flags
	 *            A list of flags that decide this adapter's behavior.
	 */
	public SessionCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	/**
	 * Mutator method for OnSessionChangeListener.
	 * 
	 * @param mListener
	 *            The OnSessionChangeListener that will be notified when a
	 *            session changes.
	 */
	public void setOnSessionChangeListener(OnSessionChangeListener mListener) {
		this.m_listener = mListener;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {

		// Create a session from the cursor provided and then bind it to an
		// existing SessionView
		Session session;
		try {
			session = new Session(
					arg2.getString(TimeToTrainTables.SESSIONS_COL_NAME),
					Session.SESSION_DATE_FORMAT.parse(arg2
							.getString(TimeToTrainTables.SESSIONS_COL_DATE)),
					arg2.getLong(TimeToTrainTables.SESSIONS_COL_ID));
		} catch (ParseException e) {
			Log.e(this.getClass().getName(), "Error parsing Date String.");
			e.printStackTrace();
			return;
		}
		SessionView view = (SessionView) arg0;
		view.setOnSessionChangeListener(null);
		view.setSession(session);

		// Set the OnSessionChangeListener
		view.setOnSessionChangeListener(this.m_listener);

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {

		// Create a session from the cursor provided
		Session session;
		try {
			session = new Session(
					arg1.getString(TimeToTrainTables.SESSIONS_COL_NAME),
					Session.SESSION_DATE_FORMAT.parse(arg1
							.getString(TimeToTrainTables.SESSIONS_COL_DATE)),
					arg1.getLong(TimeToTrainTables.SESSIONS_COL_ID));
		} catch (ParseException e) {
			Log.e(this.getClass().getName(), "Error parsing Date String.");
			e.printStackTrace();
			return null;
		}

		// Create a new SessionView and bind the Session to it
		SessionView view = new SessionView(arg0, session);

		// Set the Listener and return the view
		view.setOnSessionChangeListener(this.m_listener);
		return view;
	}

}

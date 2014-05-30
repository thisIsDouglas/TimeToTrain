package edu.calpoly.dfjimene.session;

import edu.calpoly.dfjimene.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Session View wrapper class for Sessions to be manipulated by the user
 * 
 * @author Douglas Jimenez
 * 
 */
public class SessionView extends LinearLayout {

	/** TextView of the SessionView */
	private TextView m_vwSessionName;

	/** Underlying Session */
	private Session m_session;

	/** Listener in case the session name changes */
	private OnSessionChangeListener m_onSessionChangeListener;

	/**
	 * Constructor for SessionView object. Sets internal session to the
	 * specified session
	 */
	public SessionView(Context context, Session session) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.session_view, this, true);
		this.m_vwSessionName = (TextView) findViewById(R.id.SessionText);
		this.m_onSessionChangeListener = null;
		this.setSession(session);
	}

	/**
	 * Mutator for the internal session
	 * 
	 * @param session
	 *            The session object which this View will display.
	 */
	public void setSession(Session session) {
		this.m_session = session;
		if (session.getSessionName().equals("")) {
			if (session.getSessionDate() != null) {
				this.m_vwSessionName.setText("Session on "
						+ Session.SESSION_DATE_FORMAT.format(session
								.getSessionDate()));
			}
		} else {
			if (session.getSessionDate() != null)
				this.m_vwSessionName.setText(session.getSessionName()
						+ " ("
						+ Session.SESSION_DATE_FORMAT.format(session
								.getSessionDate()) + ")");
		}
	}

	/**
	 * Mutator to set OnChangeListener for this Session
	 * 
	 * @param listener
	 *            OnSessionChangeListener
	 */
	public void setOnSessionChangeListener(OnSessionChangeListener listener) {
		this.m_onSessionChangeListener = listener;
	}

	public void notifyOnSessionChangeListener() {
		if (this.m_onSessionChangeListener != null) {
			this.m_onSessionChangeListener.onSessionChanged(this,
					this.m_session);
		}
	}

	/**
	 * Accessor for session contained by view
	 * 
	 * @return the session
	 */
	public Session getSession() {
		return this.m_session;
	}

	/**
	 * Interface definition for a callback to be invoked when the underlying
	 * Session is changed in this SessionView object.
	 */
	public static interface OnSessionChangeListener {

		/**
		 * Called when the underlying Session in a SessionView object changes
		 * state.
		 * 
		 * @param view
		 *            The SessionView in which the session was changed.
		 * @param session
		 *            The Session that was changed.
		 */
		public void onSessionChanged(SessionView view, Session session);
	}

}

package edu.calpoly.dfjimene.exerciseentry;

/**
 * Class for a simple exercise entry. We do not need a detailed entry for the
 * session details activity, so we can tune our queries to be more appropriate
 * for the situation
 * 
 * @author Douglas Jimenez
 * 
 */
public class SimpleEntry {

	/** Exercise ID */
	// private long m_exerciseID;

	/** Exercise name */
	private String m_exerciseName;

	/** Entry ID */
	private long m_entryID;

	/** Session ID */
	private long m_sessionID;

	/** Type of exercise */
	private int m_nType;

	/**
	 * Default constructor for SimpleEntry
	 */
	public SimpleEntry() {
		this.setSessionID(this.setEntryID(0));
		this.setExerciseName("");
	}

	/**
	 * Constructor for the SimpleEntry setting all IDs and the exercise name for
	 * the view to use
	 * 
	 * @param nSessionID
	 *            the session ID
	 * @param nExerciseID
	 *            the exercise ID
	 * @param strExerciseName
	 *            the exercise name
	 * @param variant
	 *            the variant of exercise (if any)
	 * @param nEntryID
	 *            the ID of the entry
	 */
	public SimpleEntry(long nSessionID, String strExerciseName, long nEntryID,
			int type) {
		this.m_entryID = nEntryID;
		this.setExerciseName(strExerciseName);
		this.setSessionID(nSessionID);
		this.setType(type);
	}

	/**
	 * Getter for entry ID
	 * 
	 * @return the entry ID
	 */
	public long getEntryID() {
		return m_entryID;
	}

	/**
	 * Setter for the entry ID
	 * 
	 * @param m_entryID
	 *            the entry ID to set
	 * @return the new entry ID
	 */
	public long setEntryID(long m_entryID) {
		this.m_entryID = m_entryID;
		return m_entryID;
	}

	/**
	 * Getter for exercise name
	 * 
	 * @return the exercise name
	 */
	public String getExerciseName() {
		return m_exerciseName;
	}

	/**
	 * Setter for exercise name
	 * 
	 * @param m_exerciseName
	 *            the exercise name
	 */
	public void setExerciseName(String m_exerciseName) {
		this.m_exerciseName = m_exerciseName;
	}

	/**
	 * Getter for session ID
	 * 
	 * @return the session ID
	 */
	public long getSessionID() {
		return m_sessionID;
	}

	/**
	 * Setter for session ID
	 * 
	 * @param m_sessionID
	 *            the new session ID
	 * @return the new session ID
	 */
	public long setSessionID(long m_sessionID) {
		this.m_sessionID = m_sessionID;
		return m_sessionID;
	}

	/**
	 * Getter for exercise type
	 * 
	 * @return the exercise type
	 */
	public int getType() {
		return m_nType;
	}

	/**
	 * Setter for exercise type
	 * 
	 * @param m_nType
	 *            the exercise type
	 */
	public void setType(int m_nType) {
		this.m_nType = m_nType;
	}

}

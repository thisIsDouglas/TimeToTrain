package edu.calpoly.dfjimene.exerciseentry;

/**
 * ExerciseEntry class. Contains all fields relevant to entries. The type
 * determines which fields will contain data. Other fields will not contain
 * anything if it is not part of the type.
 * 
 * @author Douglas Jimenez
 * 
 */
public class ExerciseEntry {
	/** Entry type identifier cardio */
	public static final int TYPE_CARDIO = 0;

	/** Entry type identifier strength */
	public static final int TYPE_STRENGTH = 1;

	/** Entry ID */
	private long m_nEntryId;

	/** Session ID */
	private long m_nSessionId;

	/** Exercise Name */
	private String m_strExreciseName;

	/** Exercise Type */
	private int type;

	/** Sets */
	private String m_strSets;

	/** Time */
	private int m_nTime;

	/** Distance */
	private double m_nDistance;

	/** Comments */
	private String m_strComments;

	/**
	 * The only important ExerciseEntry constructor. This forces any class that
	 * makes an entry at least has the arguments requested.
	 * 
	 * @param entryId
	 *            the entry ID
	 * @param sessionId
	 *            the session ID
	 * @param exerciseName
	 *            the exercise name
	 * @param type
	 *            the exercise type
	 */
	public ExerciseEntry(long entryId, long sessionId, String exerciseName,
			int type) {

		// Type must be strength or cardio at the moment
		if (type != TYPE_CARDIO && type != TYPE_STRENGTH)
			throw new IllegalArgumentException("Invalid type");

		// Initialize members
		this.setEntryId(entryId);
		this.setSessionId(sessionId);
		this.setExreciseName(exerciseName);
		this.setType(type);
	}

	/**
	 * Getter for entry ID
	 * 
	 * @return the entry ID
	 */
	public long getEntryId() {
		return m_nEntryId;
	}

	/**
	 * Setter for entry ID
	 * 
	 * @param m_nEntryId
	 *            the entry ID
	 */
	public void setEntryId(long m_nEntryId) {
		this.m_nEntryId = m_nEntryId;
	}

	/**
	 * Getter for the session ID
	 * 
	 * @return the session ID
	 */
	public long getSessionId() {
		return m_nSessionId;
	}

	/**
	 * Setter for the session ID
	 * 
	 * @param m_nSessionId
	 *            the session ID
	 */
	public void setSessionId(long m_nSessionId) {
		this.m_nSessionId = m_nSessionId;
	}

	/**
	 * Getter for the exercise name
	 * 
	 * @return the exercise name
	 */
	public String getExreciseName() {
		return m_strExreciseName;
	}

	/**
	 * Setter for the exercise name
	 * 
	 * @param m_strExreciseName
	 *            the exercise name
	 */
	public void setExreciseName(String m_strExreciseName) {
		this.m_strExreciseName = m_strExreciseName;
	}

	/**
	 * Getter for type
	 * 
	 * @return the exercise type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Setter for type
	 * 
	 * @param type
	 *            the exercise type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Getter for sets
	 * 
	 * @return the sets string
	 */
	public String getSets() {
		return m_strSets;
	}

	/**
	 * Setter for sets
	 * 
	 * @param m_strSets
	 *            the sets string
	 */
	public void setSets(String m_strSets) {
		this.m_strSets = m_strSets;
	}

	/**
	 * Getter for time
	 * 
	 * @return the time
	 */
	public int getTime() {
		return m_nTime;
	}

	/**
	 * Setter for time
	 * 
	 * @param m_nTime
	 *            the time
	 */
	public void setTime(int m_nTime) {
		this.m_nTime = m_nTime;
	}

	/**
	 * Getter for distance
	 * 
	 * @return the distance
	 */
	public double getDistance() {
		return m_nDistance;
	}

	/**
	 * Setter for distance
	 * 
	 * @param m_nDistance
	 *            the distance
	 */
	public void setDistance(double m_nDistance) {
		this.m_nDistance = m_nDistance;
	}

	/**
	 * Getter for the comments
	 * 
	 * @return the comments
	 */
	public String getComments() {
		return m_strComments;
	}

	/**
	 * Setter for comments
	 * 
	 * @param m_strComments
	 *            the comments
	 */
	public void setComments(String m_strComments) {
		this.m_strComments = m_strComments;
	}
}

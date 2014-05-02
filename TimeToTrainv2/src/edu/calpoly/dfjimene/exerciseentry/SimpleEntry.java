package edu.calpoly.dfjimene.exerciseentry;

/**
 * Class for a simple exercise entry. We do not need a detailed entry for the
 * session details activity, so we can tune our queries to be more appropriate
 * for the situation
 * 
 * @author Doug Jimenez
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
   
   /** Variant of exercise */
   //private String m_variant;

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
    * @param nSessionID the session ID
    * @param nExerciseID the exercise ID
    * @param strExerciseName the exercise name
    * @param variant the variant of exercise (if any)
    * @param nEntryID the ID of the entry
    */
   public SimpleEntry(long nSessionID,
         String strExerciseName, long nEntryID) {
      this.m_entryID = nEntryID;
      this.setExerciseName(strExerciseName);
      this.setSessionID(nSessionID);
   }

   public long getEntryID() {
      return m_entryID;
   }

   public long setEntryID(long m_entryID) {
      this.m_entryID = m_entryID;
      return m_entryID;
   }

   public String getExerciseName() {
      return m_exerciseName;
   }

   public void setExerciseName(String m_exerciseName) {
      this.m_exerciseName = m_exerciseName;
   }

   public long getSessionID() {
      return m_sessionID;
   }

   public long setSessionID(long m_sessionID) {
      this.m_sessionID = m_sessionID;
      return m_sessionID;
   }


}

package edu.calpoly.dfjimene.exerciseentry;

public class ExerciseEntry {
   /** Entry type identifiers */
   public static final int TYPE_CARDIO = 0;
   public static final int TYPE_STRENGTH = 1;
   
   private long m_nEntryId;
   private long m_nSessionId;
   private String m_strExreciseName;
   private int type;
   private String m_strSets;
   private int m_nTime;
   private double m_nDistance;
   private String m_strComments;
   
   public ExerciseEntry(long entryId, long sessionId, String exerciseName, int type){
      if(type != TYPE_CARDIO && type != TYPE_STRENGTH)
         throw new IllegalArgumentException("Invalid type");
      this.setEntryId(entryId);
      this.setSessionId(sessionId);
      this.setExreciseName(exerciseName);
      this.setType(type);
   }

   public long getEntryId() {
      return m_nEntryId;
   }

   public void setEntryId(long m_nEntryId) {
      this.m_nEntryId = m_nEntryId;
   }

   public long getSessionId() {
      return m_nSessionId;
   }

   public void setSessionId(long m_nSessionId) {
      this.m_nSessionId = m_nSessionId;
   }

   public String getExreciseName() {
      return m_strExreciseName;
   }

   public void setExreciseName(String m_strExreciseName) {
      this.m_strExreciseName = m_strExreciseName;
   }

   public int getType() {
      return type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getSets() {
      return m_strSets;
   }

   public void setSets(String m_strSets) {
      this.m_strSets = m_strSets;
   }

   public int getTime() {
      return m_nTime;
   }

   public void setTime(int m_nTime) {
      this.m_nTime = m_nTime;
   }

   public double getDistance() {
      return m_nDistance;
   }

   public void setDistance(double m_nDistance) {
      this.m_nDistance = m_nDistance;
   }

   public String getComments() {
      return m_strComments;
   }

   public void setComments(String m_strComments) {
      this.m_strComments = m_strComments;
   }
}

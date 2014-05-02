package edu.calpoly.dfjimene.session;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Session {

   public static final SimpleDateFormat SESSION_DATE_FORMAT = new SimpleDateFormat(
         "EEE, MMM dd, yyyy", Locale.US);

   /** Name of Session (If it has one) */
   private String m_strName;

   /** Id of Session */
   private long m_nId;

   /** Date of Session */
   private Date m_dateSession;

   /**
    * Initializes a blank session.
    */
   public Session() {
      this.m_strName = "";
      this.m_nId = 0;
      this.m_dateSession = Calendar.getInstance().getTime();
   }
   
   /**
    * Initializes the session with only a date and ID
    * 
    * @param date
    *           Date of session.
    * 
    * @param id
    *           The session ID.
    */
   public Session(String name) {
      this.m_dateSession = Calendar.getInstance().getTime();
      this.m_nId = 0;
      this.m_strName = name;
   }

   /**
    * Initializes with a session with all parameters filled in.
    * 
    * @param strName
    *           Name of workout session.
    * 
    * @param date
    *           Date of workout session.
    * 
    * @param id
    *           Session ID.
    */
   public Session(String strName, Date date, long id) {
      this.m_dateSession = date;
      this.m_nId = id;
      this.m_strName = strName;
   }

   /**
    * Accessor for the session name.
    * 
    * @return A String value containing the name of the session.
    */
   public String getSessionName() {
      return this.m_strName;
   }

   /**
    * Mutator that changes the session name.
    * 
    * @param strJoke
    *           The new session name.
    */
   public void setName(String strName) {
      this.m_strName = strName;
   }

   /**
    * Accessor for the date of this session.
    * 
    * @return A Date for the session.
    */
   public Date getSessionDate() {
      return this.m_dateSession;
   }

   /**
    * Mutator that changes the Date of this session.
    * 
    * @param dateSession
    *           A date.
    */
   public void setAuthor(Date dateSession) {
      this.m_dateSession = dateSession;
   }

   /**
    * Accessor for the unique ID of this session.
    * 
    * @return an long set to the unique id of this session.
    */
   public long getID() {
      return this.m_nId;
   }

   /**
    * Mutator that changes the unique id of this session.
    * 
    * @param id
    *           A long representing the unique id for this session.
    */
   public void setId(long id) {
      this.m_nId = id;
   }

   /**
    * Returns only the text of the joke. This method should mimic getJoke().
    * 
    * @return A string containing the text of the joke
    */
   @Override
   public String toString() {
      if (this.m_strName.equals(""))
         return SESSION_DATE_FORMAT.format(m_dateSession);
      else
         return m_strName;
   }

   /**
    * An Object is equal to this Session if all items below are true:
    * 
    * 1) The Object is a Session.
    * 
    * 2) The Session's id is the same as this Session's id.
    * 
    * @return True if the object passed in is a Session with the same id as this
    *         one; False otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Session) {
         Session session2 = (Session) obj;
         return session2.getID() == this.m_nId;
      }
      return false;
   }
}

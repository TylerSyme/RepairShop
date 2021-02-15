package ca.mcgill.ecse321.repairshop.model;

public class Reminder {

    //------------------------
    // ENUMERATIONS
    //------------------------

    //Reminder Attributes
    private Long reminderID;

    //------------------------
    // MEMBER VARIABLES
    //------------------------
    private String dateTime;
    private ReminderType reminderType;
    //Reminder Associations
    private Customer customer;

    public Reminder(Long aReminderID, String aDateTime, ReminderType aReminderType, Customer aCustomer) {
        reminderID = aReminderID;
        dateTime = aDateTime;
        reminderType = aReminderType;
        boolean didAddCustomer = setCustomer(aCustomer);
        if (!didAddCustomer) {
            throw new RuntimeException("Unable to create reminder due to customer. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public boolean setReminderID(Long aReminderID) {
        boolean wasSet = false;
        reminderID = aReminderID;
        wasSet = true;
        return wasSet;
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setDateTime(String aDateTime) {
        boolean wasSet = false;
        dateTime = aDateTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setReminderType(ReminderType aReminderType) {
        boolean wasSet = false;
        reminderType = aReminderType;
        wasSet = true;
        return wasSet;
    }

    public Long getReminderID() {
        return reminderID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    /* Code from template association_GetOne */
    public Customer getCustomer() {
        return customer;
    }

    /* Code from template association_SetOneToMany */
    public boolean setCustomer(Customer aCustomer) {
        boolean wasSet = false;
        if (aCustomer == null) {
            return wasSet;
        }

        Customer existingCustomer = customer;
        customer = aCustomer;
        if (existingCustomer != null && !existingCustomer.equals(aCustomer)) {
            existingCustomer.removeReminder(this);
        }
        customer.addReminder(this);
        wasSet = true;
        return wasSet;
    }

    public void delete() {
        Customer placeholderCustomer = customer;
        this.customer = null;
        if (placeholderCustomer != null) {
            placeholderCustomer.removeReminder(this);
        }
    }

    public String toString() {
        return super.toString() + "[" +
                "reminderID" + ":" + getReminderID() + "," +
                "dateTime" + ":" + getDateTime() + "," +
                "  " + "reminderType" + "=" + (getReminderType() != null ? !getReminderType().equals(this) ? getReminderType().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "customer = " + (getCustomer() != null ? Integer.toHexString(System.identityHashCode(getCustomer())) : "null");
    }


    public enum ReminderType {OilChange, Confirmation, Maintenance, RegularCheckups}
}
/** Donor class: stores the details of one blood donor. */
public class Donor {
    private String id, name, bloodGroup, contact, city;
    private String lastDonation;   // "" if the donor has never donated
    private int age;

    // Constructor: runs when we write  new Donor(...)
    public Donor(String id, String name, int age, String bloodGroup, String contact, String city) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.contact = contact;
        this.city = city;
        this.lastDonation = "";
    }

    // Getters (read the values)
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getBloodGroup() { return bloodGroup; }
    public String getContact() { return contact; }
    public String getCity() { return city; }
    public String getLastDonation() { return lastDonation; }

    // Setters (change the values - used for Update)
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public void setContact(String contact) { this.contact = contact; }
    public void setCity(String city) { this.city = city; }
    public void setLastDonation(String lastDonation) { this.lastDonation = lastDonation; }
}

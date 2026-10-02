/** Appointment class: a donor booked for a camp. */
public class Appointment {
    private String id, donorId, campId;

    public Appointment(String id, String donorId, String campId) {
        this.id = id;
        this.donorId = donorId;
        this.campId = campId;
    }

    public String getId() { return id; }
    public String getDonorId() { return donorId; }
    public String getCampId() { return campId; }

    public void setCampId(String campId) { this.campId = campId; }
}

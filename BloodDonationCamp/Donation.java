/** Donation class: one completed blood donation (1 unit = 350 ml). */
public class Donation {
    private String id, donorId, campId, date;

    public Donation(String id, String donorId, String campId, String date) {
        this.id = id;
        this.donorId = donorId;
        this.campId = campId;
        this.date = date;
    }

    public String getId() { return id; }
    public String getDonorId() { return donorId; }
    public String getCampId() { return campId; }
    public String getDate() { return date; }
}

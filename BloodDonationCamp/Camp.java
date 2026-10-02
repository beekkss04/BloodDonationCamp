/** Camp class: one blood donation camp. */
public class Camp {
    private String id, name, location, date;   // date looks like 2026-12-25

    public Camp(String id, String name, String location, String date) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDate() { return date; }

    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(String date) { this.date = date; }
}

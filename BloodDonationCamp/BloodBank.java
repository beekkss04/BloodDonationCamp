import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * BloodBank: keeps all the data and does all the work
 * (add / update / delete, search, sort, reports).
 * The GUI (MainGUI.java) only calls the methods of this class.
 */
public class BloodBank {

    // ---------- Our collections ----------
    private ArrayList<Donor> donors = new ArrayList<>();               // ArrayList : donors
    private ArrayList<Camp> camps = new ArrayList<>();                 // ArrayList : camps
    private ArrayList<Appointment> appointments = new ArrayList<>();   // ArrayList : appointments
    private LinkedList<Donation> history = new LinkedList<>();         // LinkedList: donation history
    private HashMap<String, Donor> donorMap = new HashMap<>();         // HashMap   : donor ID -> Donor
    private int[] stock = new int[Validator.BLOOD_GROUPS.length];      // Array     : units per blood group

    // Counters used to create IDs like D001, C001, A001, N001
    private int donorNo = 0, campNo = 0, apptNo = 0, donationNo = 0;

    public ArrayList<Donor> getDonors() { return donors; }
    public ArrayList<Camp> getCamps() { return camps; }
    public ArrayList<Appointment> getAppointments() { return appointments; }
    public LinkedList<Donation> getHistory() { return history; }

    // ================= DONOR (CRUD) =================

    public void addDonor(String name, int age, String group, String contact, String city)
            throws InvalidDonationException {
        Validator.checkDonor(name, age, group, contact, city);
        donorNo++;
        Donor d = new Donor(String.format("D%03d", donorNo), name.trim(), age, group, contact.trim(), city.trim());
        donors.add(d);
        donorMap.put(d.getId(), d);
    }

    public void updateDonor(String id, String name, int age, String group, String contact, String city)
            throws InvalidDonationException {
        Donor d = needDonor(id);
        Validator.checkDonor(name, age, group, contact, city);
        d.setName(name.trim());
        d.setAge(age);
        d.setBloodGroup(group);
        d.setContact(contact.trim());
        d.setCity(city.trim());
    }

    public void deleteDonor(String id) throws InvalidDonationException {
        Donor d = needDonor(id);
        donors.remove(d);
        donorMap.remove(d.getId());
    }

    // Finds a donor using the HashMap (returns null if not found)
    public Donor findDonor(String id) {
        return donorMap.get(id.trim().toUpperCase());
    }

    // Same as findDonor but throws an exception if not found
    private Donor needDonor(String id) throws InvalidDonationException {
        Donor d = findDonor(id);
        if (d == null) throw new InvalidDonationException("Donor '" + id.trim() + "' does not exist.");
        return d;
    }

    // ================= SEARCH =================

    // Linear search: check every donor one by one
    public ArrayList<Donor> searchByGroup(String group) {
        ArrayList<Donor> result = new ArrayList<>();
        for (Donor d : donors) {
            if (d.getBloodGroup().equalsIgnoreCase(group.trim())) {
                result.add(d);
            }
        }
        return result;
    }

    // ================= SORT =================

    public ArrayList<Donor> sortByName() {
        ArrayList<Donor> list = new ArrayList<>(donors);
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return list;
    }

    // Latest donation first (donors who never donated come last)
    public ArrayList<Donor> sortByDonationDate() {
        ArrayList<Donor> list = new ArrayList<>(donors);
        list.sort((a, b) -> b.getLastDonation().compareTo(a.getLastDonation()));
        return list;
    }

    // TreeMap keeps its keys sorted automatically: blood group first, then name
    public Collection<Donor> sortByBloodGroup() {
        TreeMap<String, Donor> tree = new TreeMap<>();
        for (Donor d : donors) {
            tree.put(d.getBloodGroup() + " " + d.getName() + " " + d.getId(), d);
        }
        return tree.values();
    }

    // ================= CAMP (CRUD) =================

    public void addCamp(String name, String location, String date) throws InvalidDonationException {
        checkCamp(name, location, date);
        campNo++;
        camps.add(new Camp(String.format("C%03d", campNo), name.trim(), location.trim(), date.trim()));
    }

    public void updateCamp(String id, String name, String location, String date) throws InvalidDonationException {
        Camp c = needCamp(id);
        checkCamp(name, location, date);
        c.setName(name.trim());
        c.setLocation(location.trim());
        c.setDate(date.trim());
    }

    public void deleteCamp(String id) throws InvalidDonationException {
        camps.remove(needCamp(id));
    }

    private void checkCamp(String name, String location, String date) throws InvalidDonationException {
        if (name.trim().isEmpty() || location.trim().isEmpty()) {
            throw new InvalidDonationException("Camp name and location cannot be empty.");
        }
        Validator.toDate(date);
    }

    public Camp findCamp(String id) {
        for (Camp c : camps) {
            if (c.getId().equalsIgnoreCase(id.trim())) return c;
        }
        return null;
    }

    private Camp needCamp(String id) throws InvalidDonationException {
        Camp c = findCamp(id);
        if (c == null) throw new InvalidDonationException("Camp '" + id.trim() + "' does not exist.");
        return c;
    }

    // ================= APPOINTMENT (CRUD) =================

    public void bookAppointment(String donorId, String campId) throws InvalidDonationException {
        Donor d = needDonor(donorId);
        Camp c = needCamp(campId);
        LocalDate campDate = Validator.toDate(c.getDate());
        if (campDate.isBefore(LocalDate.now())) {
            throw new InvalidDonationException("This camp date has already passed.");
        }
        checkGap(d, campDate);
        for (Appointment a : appointments) {
            if (a.getDonorId().equals(d.getId()) && a.getCampId().equals(c.getId())) {
                throw new InvalidDonationException("This donor is already booked for this camp.");
            }
        }
        apptNo++;
        appointments.add(new Appointment(String.format("A%03d", apptNo), d.getId(), c.getId()));
    }

    public void updateAppointment(String id, String newCampId) throws InvalidDonationException {
        Appointment a = needAppointment(id);
        Camp c = needCamp(newCampId);
        a.setCampId(c.getId());
    }

    public void deleteAppointment(String id) throws InvalidDonationException {
        appointments.remove(needAppointment(id));
    }

    private Appointment needAppointment(String id) throws InvalidDonationException {
        for (Appointment a : appointments) {
            if (a.getId().equalsIgnoreCase(id.trim())) return a;
        }
        throw new InvalidDonationException("Appointment '" + id.trim() + "' does not exist.");
    }

    // ================= DONATION =================

    // A donor must wait 90 days between two donations
    private void checkGap(Donor d, LocalDate date) throws InvalidDonationException {
        if (!d.getLastDonation().isEmpty()) {
            long days = ChronoUnit.DAYS.between(LocalDate.parse(d.getLastDonation()), date);
            if (days < 90) {
                throw new InvalidDonationException(d.getName() + " donated only " + days
                        + " days ago. The minimum gap is 90 days.");
            }
        }
    }

    public void recordDonation(String donorId, String campId, LocalDate date) throws InvalidDonationException {
        Donor d = needDonor(donorId);
        Camp c = needCamp(campId);
        checkGap(d, date);
        donationNo++;
        history.addFirst(new Donation(String.format("N%03d", donationNo), d.getId(), c.getId(), date.toString()));
        d.setLastDonation(date.toString());
        stock[Validator.indexOf(d.getBloodGroup())]++;
    }

    // Donation history of one donor (newest first)
    public LinkedList<Donation> historyOf(String donorId) {
        LinkedList<Donation> list = new LinkedList<>();
        for (Donation n : history) {
            if (n.getDonorId().equalsIgnoreCase(donorId.trim())) list.add(n);
        }
        return list;
    }

    // ================= REPORTS AND CERTIFICATE =================

    public String overallReport() {
        String r = "======= BLOOD DONATION CAMP REPORT =======\n";
        r += "Date            : " + LocalDate.now() + "\n";
        r += "Total donors    : " + donors.size() + "\n";
        r += "Total camps     : " + camps.size() + "\n";
        r += "Appointments    : " + appointments.size() + "\n";
        r += "Total donations : " + history.size() + "\n\n";
        r += "Blood Group   Donors   Units Collected\n";
        r += "-------------------------------------\n";
        for (int i = 0; i < stock.length; i++) {
            String group = Validator.BLOOD_GROUPS[i];
            r += String.format("%-13s %-8d %d%n", group, searchByGroup(group).size(), stock[i]);
        }
        return r;
    }

    public String donorReport(String id) throws InvalidDonationException {
        Donor d = needDonor(id);
        String r = "========== DONOR REPORT ==========\n";
        r += "ID           : " + d.getId() + "\n";
        r += "Name         : " + d.getName() + "\n";
        r += "Age          : " + d.getAge() + "\n";
        r += "Blood Group  : " + d.getBloodGroup() + "\n";
        r += "Contact      : " + d.getContact() + "\n";
        r += "City         : " + d.getCity() + "\n\n";
        r += "Donation history (newest first):\n";
        LinkedList<Donation> list = historyOf(id);
        if (list.isEmpty()) r += "  No donations yet.\n";
        for (Donation n : list) {
            r += "  " + n.getId() + " | " + n.getDate() + " | Camp " + n.getCampId() + "\n";
        }
        return r;
    }

    public String certificate(String id) throws InvalidDonationException {
        Donor d = needDonor(id);
        LinkedList<Donation> list = historyOf(id);
        if (list.isEmpty()) {
            throw new InvalidDonationException(d.getName() + " has not donated yet, so no certificate can be given.");
        }
        Donation last = list.getFirst();
        Camp c = findCamp(last.getCampId());
        String campName = (c == null) ? last.getCampId() : c.getName();
        return "**************************************************\n"
             + "*                                                *\n"
             + "*          CERTIFICATE OF APPRECIATION           *\n"
             + "*                                                *\n"
             + "**************************************************\n\n"
             + "   This certificate is proudly presented to\n\n"
             + "        " + d.getName().toUpperCase() + "\n\n"
             + "   for donating blood (Group " + d.getBloodGroup() + ")\n"
             + "   at " + campName + " on " + last.getDate() + ".\n\n"
             + "   \"Your gift of blood is a gift of life.\"\n\n"
             + "   Certificate No: " + last.getId() + "     Donor ID: " + d.getId() + "\n\n"
             + "   ____________________          ____________________\n"
             + "   Camp Coordinator               Medical Officer\n";
    }

    // ================= DEMO DATA =================

    public void loadSampleData() {
        try {
            LocalDate today = LocalDate.now();
            addDonor("Rahul Sharma", 25, "O+", "9876543210", "Mumbai");
            addDonor("Priya Patel", 22, "A+", "9823456781", "Pune");
            addDonor("Amit Verma", 31, "B+", "9812345670", "Delhi");
            addDonor("Sneha Iyer", 28, "AB-", "9934567812", "Chennai");
            addDonor("Karan Mehta", 35, "O-", "9765432109", "Ahmedabad");
            addDonor("Anjali Singh", 24, "A-", "9654321098", "Lucknow");

            addCamp("City Hospital Camp", "Mumbai", today.minusDays(120).toString());
            addCamp("University Blood Drive", "ITM Skills University", today.plusDays(10).toString());
            addCamp("Red Cross Camp", "Pune", today.plusDays(30).toString());

            recordDonation("D001", "C001", today.minusDays(120));
            recordDonation("D002", "C001", today.minusDays(118));
            recordDonation("D005", "C001", today.minusDays(110));
            bookAppointment("D003", "C002");
            bookAppointment("D001", "C002");
        } catch (InvalidDonationException e) {
            System.out.println("Demo data error: " + e.getMessage());
        }
    }
}

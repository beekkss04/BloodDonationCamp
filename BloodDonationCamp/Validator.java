import java.time.LocalDate;

/** Validator: all the input checks are here. */
public class Validator {

    // Array of the 8 blood groups
    public static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    // Position of a blood group in the array (-1 if not found)
    public static int indexOf(String group) {
        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            if (BLOOD_GROUPS[i].equalsIgnoreCase(group)) return i;
        }
        return -1;
    }

    public static void checkBloodGroup(String group) throws InvalidDonationException {
        if (indexOf(group) == -1) {
            throw new InvalidDonationException("Invalid blood group. Use A+, A-, B+, B-, AB+, AB-, O+ or O-.");
        }
    }

    // Checks everything about a donor
    public static void checkDonor(String name, int age, String group, String contact, String city)
            throws InvalidDonationException {
        if (!name.trim().matches("[A-Za-z ]{2,30}")) {
            throw new InvalidDonationException("Name must have only letters (2 to 30 characters).");
        }
        if (age < 18 || age > 65) {
            throw new InvalidDonationException("Donor age must be between 18 and 65.");
        }
        checkBloodGroup(group);
        if (!contact.trim().matches("[0-9]{10}")) {
            throw new InvalidDonationException("Contact must be exactly 10 digits.");
        }
        if (city.trim().isEmpty()) {
            throw new InvalidDonationException("City cannot be empty.");
        }
    }

    // Converts text like "25" to a number, or throws our exception
    public static int toNumber(String text, String field) throws InvalidDonationException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidDonationException(field + " must be a number.");
        }
    }

    // Converts text like "2026-12-25" to a date, or throws our exception
    public static LocalDate toDate(String text) throws InvalidDonationException {
        try {
            return LocalDate.parse(text.trim());
        } catch (Exception e) {
            throw new InvalidDonationException("Date must look like 2026-12-25 (yyyy-mm-dd).");
        }
    }
}

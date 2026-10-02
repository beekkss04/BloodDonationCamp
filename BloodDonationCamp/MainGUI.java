import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * MainGUI: the Swing window. It has 6 tabs.
 * Every tab has the same simple design: a form with buttons on top and a table below.
 */
public class MainGUI extends JFrame {

    BloodBank bank = new BloodBank();
    JTabbedPane tabs = new JTabbedPane();

    // ---------- Donor tab ----------
    JTextField dName = new JTextField(10), dAge = new JTextField(3);
    JTextField dContact = new JTextField(10), dCity = new JTextField(8);
    JComboBox<String> dGroup = new JComboBox<>(Validator.BLOOD_GROUPS);
    DefaultTableModel donorModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Age", "Blood Group", "Contact", "City", "Last Donation"}, 0);
    JTable donorTable = makeTable(donorModel);

    // ---------- Camp tab ----------
    JTextField cName = new JTextField(12), cLocation = new JTextField(10);
    JTextField cDate = new JTextField(LocalDate.now().plusDays(7).toString(), 8);
    DefaultTableModel campModel = new DefaultTableModel(new String[]{"ID", "Camp Name", "Location", "Date"}, 0);
    JTable campTable = makeTable(campModel);

    // ---------- Appointment tab ----------
    JTextField aDonor = new JTextField(6), aCamp = new JTextField(6);
    DefaultTableModel apptModel = new DefaultTableModel(new String[]{"Appointment ID", "Donor ID", "Camp ID"}, 0);
    JTable apptTable = makeTable(apptModel);

    // ---------- Donation tab ----------
    JTextField nDonor = new JTextField(6), nCamp = new JTextField(6);
    DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"Donation ID", "Donor ID", "Camp ID", "Date"}, 0);
    JTable historyTable = makeTable(historyModel);

    // ---------- Search and Sort tab ----------
    JTextField sText = new JTextField(8);
    JComboBox<String> sType = new JComboBox<>(new String[]{"Donor ID", "Blood Group"});
    DefaultTableModel searchModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Age", "Blood Group", "Contact", "City", "Last Donation"}, 0);
    JTable searchTable = makeTable(searchModel);

    // ---------- Report tab ----------
    JTextField rDonor = new JTextField(6);
    JTextArea reportArea = new JTextArea();

    public MainGUI() {
        setTitle("Blood Donation Camp Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Red title bar at the top
        JLabel title = new JLabel("Blood Donation Camp Management System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setOpaque(true);
        title.setBackground(new Color(183, 28, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        tabs.add("Donors", donorTab());
        tabs.add("Camps", campTab());
        tabs.add("Appointments", appointmentTab());
        tabs.add("Donations", donationTab());
        tabs.add("Search & Sort", searchTab());
        tabs.add("Reports", reportTab());

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        bank.loadSampleData();
        refreshAll();
    }

    // ==================== SMALL HELPERS ====================

    JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setDefaultEditor(Object.class, null);   // user cannot type inside the table
        t.setRowHeight(24);
        return t;
    }

    // Puts the form on top and the table below it
    JPanel makeTab(JPanel form, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    void msg(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    // Returns the ID (first column) of the selected table row
    String selectedId(JTable table) throws InvalidDonationException {
        int row = table.getSelectedRow();
        if (row == -1) throw new InvalidDonationException("Please click a row in the table first.");
        return table.getValueAt(row, 0).toString();
    }

    void fillDonors(DefaultTableModel model, Collection<Donor> list) {
        model.setRowCount(0);
        for (Donor d : list) {
            String last = d.getLastDonation().isEmpty() ? "-" : d.getLastDonation();
            model.addRow(new Object[]{d.getId(), d.getName(), d.getAge(), d.getBloodGroup(),
                    d.getContact(), d.getCity(), last});
        }
    }

    void fillHistory(Collection<Donation> list) {
        historyModel.setRowCount(0);
        for (Donation n : list) {
            historyModel.addRow(new Object[]{n.getId(), n.getDonorId(), n.getCampId(), n.getDate()});
        }
    }

    // Reloads every table from the data
    void refreshAll() {
        fillDonors(donorModel, bank.getDonors());
        fillDonors(searchModel, bank.getDonors());
        fillHistory(bank.getHistory());

        campModel.setRowCount(0);
        for (Camp c : bank.getCamps()) {
            campModel.addRow(new Object[]{c.getId(), c.getName(), c.getLocation(), c.getDate()});
        }
        apptModel.setRowCount(0);
        for (Appointment a : bank.getAppointments()) {
            apptModel.addRow(new Object[]{a.getId(), a.getDonorId(), a.getCampId()});
        }
    }

    void saveToFile(String folder, String fileName, String text) {
        try {
            new File(folder).mkdirs();
            PrintWriter out = new PrintWriter(folder + "/" + fileName);
            out.print(text);
            out.close();
            msg("Saved in: " + folder + "/" + fileName);
        } catch (IOException e) {
            msg("Could not save the file.");
        }
    }

    // ==================== DONOR TAB ====================

    JPanel donorTab() {
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");

        add.addActionListener(e -> {
            try {
                bank.addDonor(dName.getText(), Validator.toNumber(dAge.getText(), "Age"),
                        (String) dGroup.getSelectedItem(), dContact.getText(), dCity.getText());
                refreshAll();
                msg("Donor added.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        update.addActionListener(e -> {
            try {
                bank.updateDonor(selectedId(donorTable), dName.getText(), Validator.toNumber(dAge.getText(), "Age"),
                        (String) dGroup.getSelectedItem(), dContact.getText(), dCity.getText());
                refreshAll();
                msg("Donor updated.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            try {
                bank.deleteDonor(selectedId(donorTable));
                refreshAll();
                msg("Donor deleted.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        // When a row is clicked, copy its values into the form
        donorTable.getSelectionModel().addListSelectionListener(e -> {
            int r = donorTable.getSelectedRow();
            if (r != -1) {
                dName.setText(donorTable.getValueAt(r, 1).toString());
                dAge.setText(donorTable.getValueAt(r, 2).toString());
                dGroup.setSelectedItem(donorTable.getValueAt(r, 3));
                dContact.setText(donorTable.getValueAt(r, 4).toString());
                dCity.setText(donorTable.getValueAt(r, 5).toString());
            }
        });

        JPanel form = new JPanel();
        form.add(new JLabel("Name:"));    form.add(dName);
        form.add(new JLabel("Age:"));     form.add(dAge);
        form.add(new JLabel("Blood Group:")); form.add(dGroup);
        form.add(new JLabel("Contact:")); form.add(dContact);
        form.add(new JLabel("City:"));    form.add(dCity);
        form.add(add); form.add(update); form.add(delete);
        return makeTab(form, donorTable);
    }

    // ==================== CAMP TAB ====================

    JPanel campTab() {
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");

        add.addActionListener(e -> {
            try {
                bank.addCamp(cName.getText(), cLocation.getText(), cDate.getText());
                refreshAll();
                msg("Camp added.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        update.addActionListener(e -> {
            try {
                bank.updateCamp(selectedId(campTable), cName.getText(), cLocation.getText(), cDate.getText());
                refreshAll();
                msg("Camp updated.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            try {
                bank.deleteCamp(selectedId(campTable));
                refreshAll();
                msg("Camp deleted.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        campTable.getSelectionModel().addListSelectionListener(e -> {
            int r = campTable.getSelectedRow();
            if (r != -1) {
                cName.setText(campTable.getValueAt(r, 1).toString());
                cLocation.setText(campTable.getValueAt(r, 2).toString());
                cDate.setText(campTable.getValueAt(r, 3).toString());
            }
        });

        JPanel form = new JPanel();
        form.add(new JLabel("Camp Name:")); form.add(cName);
        form.add(new JLabel("Location:"));  form.add(cLocation);
        form.add(new JLabel("Date (yyyy-mm-dd):")); form.add(cDate);
        form.add(add); form.add(update); form.add(delete);
        return makeTab(form, campTable);
    }

    // ==================== APPOINTMENT TAB ====================

    JPanel appointmentTab() {
        JButton book = new JButton("Book");
        JButton change = new JButton("Change Camp");
        JButton delete = new JButton("Delete");

        book.addActionListener(e -> {
            try {
                bank.bookAppointment(aDonor.getText(), aCamp.getText());
                refreshAll();
                msg("Appointment booked.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        change.addActionListener(e -> {
            try {
                bank.updateAppointment(selectedId(apptTable), aCamp.getText());
                refreshAll();
                msg("Appointment changed to the new camp.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            try {
                bank.deleteAppointment(selectedId(apptTable));
                refreshAll();
                msg("Appointment deleted.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        JPanel form = new JPanel();
        form.add(new JLabel("Donor ID (e.g. D001):")); form.add(aDonor);
        form.add(new JLabel("Camp ID (e.g. C002):"));  form.add(aCamp);
        form.add(book); form.add(change); form.add(delete);
        return makeTab(form, apptTable);
    }

    // ==================== DONATION TAB ====================

    JPanel donationTab() {
        JButton record = new JButton("Record Donation (today)");
        JButton showDonor = new JButton("Show History of Donor");
        JButton showAll = new JButton("Show All");

        record.addActionListener(e -> {
            try {
                bank.recordDonation(nDonor.getText(), nCamp.getText(), LocalDate.now());
                refreshAll();
                msg("Donation recorded. Thank you!");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        showDonor.addActionListener(e -> {
            if (bank.findDonor(nDonor.getText()) == null) {
                msg("Donor '" + nDonor.getText().trim() + "' does not exist.");
            } else {
                fillHistory(bank.historyOf(nDonor.getText()));
            }
        });

        showAll.addActionListener(e -> fillHistory(bank.getHistory()));

        JPanel form = new JPanel();
        form.add(new JLabel("Donor ID:")); form.add(nDonor);
        form.add(new JLabel("Camp ID:"));  form.add(nCamp);
        form.add(record); form.add(showDonor); form.add(showAll);
        return makeTab(form, historyTable);
    }

    // ==================== SEARCH AND SORT TAB ====================

    JPanel searchTab() {
        JButton search = new JButton("Search");
        JButton byName = new JButton("Sort by Name");
        JButton byDate = new JButton("Sort by Donation Date");
        JButton byGroup = new JButton("Sort by Blood Group");
        JButton all = new JButton("Show All");

        search.addActionListener(e -> {
            try {
                ArrayList<Donor> result = new ArrayList<>();
                if (sType.getSelectedIndex() == 0) {
                    Donor d = bank.findDonor(sText.getText());          // HashMap search
                    if (d != null) result.add(d);
                } else {
                    Validator.checkBloodGroup(sText.getText());
                    result = bank.searchByGroup(sText.getText());       // linear search
                }
                fillDonors(searchModel, result);
                if (result.isEmpty()) msg("No donor found.");
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        byName.addActionListener(e -> fillDonors(searchModel, bank.sortByName()));
        byDate.addActionListener(e -> fillDonors(searchModel, bank.sortByDonationDate()));
        byGroup.addActionListener(e -> fillDonors(searchModel, bank.sortByBloodGroup()));   // TreeMap
        all.addActionListener(e -> fillDonors(searchModel, bank.getDonors()));

        JPanel form = new JPanel();
        form.add(new JLabel("Search by:")); form.add(sType);
        form.add(sText); form.add(search);
        form.add(byName); form.add(byDate); form.add(byGroup); form.add(all);
        return makeTab(form, searchTable);
    }

    // ==================== REPORT TAB ====================

    JPanel reportTab() {
        JButton overall = new JButton("Overall Report");
        JButton donor = new JButton("Donor Report");
        JButton cert = new JButton("Certificate");
        JButton save = new JButton("Save Report to File");

        overall.addActionListener(e -> reportArea.setText(bank.overallReport()));

        donor.addActionListener(e -> {
            try {
                reportArea.setText(bank.donorReport(rDonor.getText()));
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        cert.addActionListener(e -> {
            try {
                String text = bank.certificate(rDonor.getText());
                reportArea.setText(text);
                saveToFile("certificates", "certificate_" + rDonor.getText().trim().toUpperCase() + ".txt", text);
            } catch (InvalidDonationException ex) {
                msg(ex.getMessage());
            }
        });

        save.addActionListener(e -> {
            if (reportArea.getText().isEmpty()) msg("Create a report first.");
            else saveToFile("reports", "report.txt", reportArea.getText());
        });

        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        reportArea.setEditable(false);

        JPanel form = new JPanel();
        form.add(new JLabel("Donor ID:")); form.add(rDonor);
        form.add(overall); form.add(donor); form.add(cert); form.add(save);

        JPanel p = new JPanel(new BorderLayout());
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return p;
    }

    // ==================== START OF THE PROGRAM ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}

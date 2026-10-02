# Blood Donation Camp Management System
Java Programming - Case Study 34 | B.Tech CSE 2025-29, Semester III | ITM Skills University
Student: Bhaumik Keer | Roll No: 150096725030

## How to run (needs JDK 11 or newer)
    cd BloodDonationCamp
    javac *.java
    java MainGUI

Mac shortcut: double-click `run.command` (first time: right-click > Open).

## Files (8)
| File | Purpose |
|---|---|
| MainGUI.java | The Swing window (6 tabs). The program starts here |
| BloodBank.java | All logic and collections: CRUD, search, sort, reports, certificate |
| Donor.java, Camp.java, Appointment.java, Donation.java | Data classes |
| Validator.java | Blood group array and input checks |
| InvalidDonationException.java | Our own exception |

Demo data loads automatically. Reports are saved in `reports/`, certificates in `certificates/`.

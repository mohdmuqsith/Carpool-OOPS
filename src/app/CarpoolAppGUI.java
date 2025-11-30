package app;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

// --------------------------- DATA CLASSES ---------------------------

abstract class User {
    protected String name;
    protected String email;
    protected String emiratesId;
    protected String gender;
    protected int points;

    public User(String name, String email, String emiratesId, String gender) {
        this.name = name;
        this.email = email;
        this.emiratesId = emiratesId;
        this.gender = gender;
        this.points = 0;
    }

    abstract void showProfile(JTextArea output);

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getEmiratesId() { return emiratesId; }
    public void addPoints(int pts) { points += pts; }
}

class Driver extends User {
    private String carType;

    public Driver(String name, String email, String emiratesId, String gender, String carType) {
        super(name, email, emiratesId, gender);
        this.carType = carType;
    }

    public String getCarType() { return carType; }

    @Override
    void showProfile(JTextArea output) {
        output.append("Driver: " + name + " | Emirates ID: " + emiratesId + " | Gender: " + gender
                + " | Car: " + carType + " | Points: " + points + "\n");
    }
}

class Passenger extends User {
    public Passenger(String name, String email, String emiratesId, String gender) {
        super(name, email, emiratesId, gender);
    }

    @Override
    void showProfile(JTextArea output) {
        output.append("Passenger: " + name + " | Emirates ID: " + emiratesId + " | Gender: " + gender
                + " | Points: " + points + "\n");
    }
}

class Ride {
    private String driverEmail, pickup, destination, carType, time, genderPref;

    public Ride(String driverEmail, String pickup, String destination, String carType, String time, String genderPref) {
        this.driverEmail = driverEmail;
        this.pickup = pickup;
        this.destination = destination;
        this.carType = carType;
        this.time = time;
        this.genderPref = genderPref;
    }

    public String getPickup() { return pickup; }
    public String getDestination() { return destination; }
    public String getCarType() { return carType; }
    public String getGenderPref() { return genderPref; }
    public String getDriverEmail() { return driverEmail; }
    public String getTime() { return time; }

    @Override
    public String toString() {
        return driverEmail + "," + pickup + "," + destination + "," + carType + "," + time + "," + genderPref;
    }
}

class Booking {
    private String passengerEmail, driverEmail, pickup, destination;

    public Booking(String passengerEmail, String driverEmail, String pickup, String destination) {
        this.passengerEmail = passengerEmail;
        this.driverEmail = driverEmail;
        this.pickup = pickup;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return passengerEmail + "," + driverEmail + "," + pickup + "," + destination;
    }
}

class FileHandler {
    private final String rideFile = "rides.txt";
    private final String bookingFile = "bookings.txt";

    public void saveRide(Ride ride) {
        try (FileWriter fw = new FileWriter(rideFile, true)) {
            fw.write(ride.toString() + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveBooking(Booking booking) {
        try (FileWriter fw = new FileWriter(bookingFile, true)) {
            fw.write(booking.toString() + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public java.util.List<Ride> readRides() {
        java.util.List<Ride> rides = new java.util.ArrayList<>();
        File file = new File(rideFile);
        if (!file.exists()) return rides;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length == 6)
                    rides.add(new Ride(d[0], d[1], d[2], d[3], d[4], d[5]));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return rides;
    }
}

class RideMatcher {
    private java.util.List<Ride> rides;

    public RideMatcher(java.util.List<Ride> rides) { this.rides = rides; }

    public java.util.List<Ride> findMatchingRides(String pickup, String dest, String car, String gender) {
        java.util.List<Ride> matches = new java.util.ArrayList<>();
        for (Ride r : rides) {
            if (r.getPickup().equalsIgnoreCase(pickup)
                    && r.getDestination().equalsIgnoreCase(dest)
                    && r.getCarType().equalsIgnoreCase(car)
                    && (r.getGenderPref().equalsIgnoreCase("any")
                    || r.getGenderPref().equalsIgnoreCase(gender))) {
                matches.add(r);
            }
        }
        return matches;
    }
}

// --------------------------- GUI CLASS ---------------------------

public class CarpoolAppGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private FileHandler fileHandler;
    private JTextArea outputArea;
    private JScrollPane scrollPane;
    private JLabel matchImageLabel;

    // Path to uploaded image
    private final String matchImagePath = "/mnt/data/6ead9b42-e281-40b5-9884-89390888cf9e.png";

    public CarpoolAppGUI() {
        setTitle("🚘 HabibiLink");
        setSize(750, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        fileHandler = new FileHandler();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(730, 200));

        matchImageLabel = new JLabel();
        matchImageLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel rolePanel = createRoleSelectionPanel();
        JPanel driverPanel = createDriverPanel();
        JPanel passengerPanel = createPassengerPanel();

        mainPanel.add(rolePanel, "role");
        mainPanel.add(driverPanel, "driver");
        mainPanel.add(passengerPanel, "passenger");

        add(mainPanel, BorderLayout.CENTER);

        // Panel for output + image
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(scrollPane, BorderLayout.CENTER);
        outputPanel.add(matchImageLabel, BorderLayout.SOUTH);
        add(outputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createRoleSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        JLabel title = new JLabel("Welcome to Carpool App!", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JButton driverBtn = new JButton("I'm a Driver");
        JButton passengerBtn = new JButton("I'm a Passenger");

        driverBtn.addActionListener(e -> cardLayout.show(mainPanel, "driver"));
        passengerBtn.addActionListener(e -> cardLayout.show(mainPanel, "passenger"));

        panel.add(title);
        panel.add(driverBtn);
        panel.add(passengerBtn);
        return panel;
    }

    private JPanel createDriverPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(9, 2, 5, 5));

        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField emiratesId = new JTextField();
        String[] genderOptions = {"Male", "Female", "Other"};
        JComboBox<String> gender = new JComboBox<>(genderOptions);
        String[] carOptions = {"Sedan", "SUV", "Hatchback", "Van"};
        JComboBox<String> carType = new JComboBox<>(carOptions);
        JTextField pickup = new JTextField();
        JTextField destination = new JTextField();
        JTextField time = new JTextField();
        String[] genderPrefOptions = {"Any", "Male", "Female"};
        JComboBox<String> genderPref = new JComboBox<>(genderPrefOptions);

        form.add(new JLabel("Name:")); form.add(name);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Emirates ID:")); form.add(emiratesId);
        form.add(new JLabel("Gender:")); form.add(gender);
        form.add(new JLabel("Car Type:")); form.add(carType);
        form.add(new JLabel("Pickup:")); form.add(pickup);
        form.add(new JLabel("Destination:")); form.add(destination);
        form.add(new JLabel("Time:")); form.add(time);
        form.add(new JLabel("Gender Pref (Any/Male/Female):")); form.add(genderPref);

        JButton postRide = new JButton("Post Ride");
        JButton homeBtn = new JButton("Home");

        postRide.addActionListener(e -> {
            Driver d = new Driver(name.getText(), email.getText(), emiratesId.getText(),
                    (String) gender.getSelectedItem(), (String) carType.getSelectedItem());
            Ride r = new Ride(d.getEmail(), pickup.getText(), destination.getText(),
                    d.getCarType(), time.getText(), (String) genderPref.getSelectedItem());
            fileHandler.saveRide(r);
            d.addPoints(10);

            outputArea.append("✅ Ride posted successfully!\n");
            d.showProfile(outputArea);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());

            // Clear input fields
            name.setText(""); email.setText(""); emiratesId.setText(""); pickup.setText("");
            destination.setText(""); time.setText("");

            // Clear image
            matchImageLabel.setIcon(null);
        });

        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, "role"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(postRide);
        bottomPanel.add(homeBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPassengerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(8, 2, 5, 5));

        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField emiratesId = new JTextField();
        String[] genderOptions = {"Male", "Female", "Other"};
        JComboBox<String> gender = new JComboBox<>(genderOptions);
        JTextField pickup = new JTextField();
        JTextField destination = new JTextField();
        String[] carOptions = {"Sedan", "SUV", "Hatchback", "Van"};
        JComboBox<String> carType = new JComboBox<>(carOptions);
        String[] genderPrefOptions = {"Any", "Male", "Female"};
        JComboBox<String> genderPref = new JComboBox<>(genderPrefOptions);

        form.add(new JLabel("Name:")); form.add(name);
        form.add(new JLabel("Email:")); form.add(email);
        form.add(new JLabel("Emirates ID:")); form.add(emiratesId);
        form.add(new JLabel("Gender:")); form.add(gender);
        form.add(new JLabel("Pickup:")); form.add(pickup);
        form.add(new JLabel("Destination:")); form.add(destination);
        form.add(new JLabel("Car Type:")); form.add(carType);
        form.add(new JLabel("Gender Pref (Any/Male/Female):")); form.add(genderPref);

        JButton searchBtn = new JButton("Search & Auto-Match");
        JButton homeBtn = new JButton("Home");

        searchBtn.addActionListener(e -> {
            Passenger p = new Passenger(name.getText(), email.getText(), emiratesId.getText(),
                    (String) gender.getSelectedItem());
            java.util.List<Ride> rides = fileHandler.readRides();
            RideMatcher matcher = new RideMatcher(rides);
            java.util.List<Ride> matches = matcher.findMatchingRides(
                    pickup.getText(), destination.getText(), (String) carType.getSelectedItem(),
                    (String) genderPref.getSelectedItem());

            outputArea.setText("");
            matchImageLabel.setIcon(null); // clear previous image

            if (matches.isEmpty()) {
                outputArea.append("❌ No matching drivers found.\n");
            } else {
                Ride matched = matches.get(0);
                Booking booking = new Booking(p.getEmail(), matched.getDriverEmail(),
                        matched.getPickup(), matched.getDestination());
                fileHandler.saveBooking(booking);
                p.addPoints(5);

                // Show match text
                outputArea.append("✅ Yalla, Match Found!\n");
                outputArea.append("Driver: " + matched.getDriverEmail() +
                        " | Route: " + matched.getPickup() + " → " + matched.getDestination() +
                        " | Car: " + matched.getCarType() + " | Time: " + matched.getTime() + "\n\n");
                p.showProfile(outputArea);

                // Show match image below text
                ImageIcon matchIcon = new ImageIcon(matchImagePath);
                matchImageLabel.setIcon(matchIcon);
            }
            outputArea.setCaretPosition(outputArea.getDocument().getLength());

            // Clear input fields
            name.setText(""); email.setText(""); emiratesId.setText(""); pickup.setText("");
            destination.setText("");
        });

        homeBtn.addActionListener(e -> cardLayout.show(mainPanel, "role"));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(searchBtn);
        bottomPanel.add(homeBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CarpoolAppGUI::new);
    }
}
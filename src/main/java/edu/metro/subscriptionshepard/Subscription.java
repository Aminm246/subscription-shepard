package edu.metro.subscriptionshepard;

// Import JPA annotations for database mapping
import jakarta.persistence.*;
// Import JsonIgnore to prevent circular references during JSON serialization
import com.fasterxml.jackson.annotation.JsonIgnore;

// Mark this class as a JPA entity so it maps to a database table
@Entity
// Specify the table name in the database as subscriptions
@Table(name = "subscriptions")
public class Subscription {

    // This is the primary key for the subscription table and is auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The name of the subscription cannot be null in the database
    @Column(nullable = false)
    private String name;

    // The price of the subscription cannot be null in the database
    @Column(nullable = false)
    private double price;

    // The duration of the subscription can be null
    @Column
    private String duration;

    // The payment frequency column in the database is named payment_frequency
    @Column(name = "payment_frequency")
    private String paymentFrequency;

    // Many subscriptions can belong to one user
    // This sets up a foreign key called user_id in the subscriptions table
    // JsonIgnore prevents infinite loops when converting to JSON
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // Breaks circular reference
    private User user;

    // Default constructor required by JPA
    public Subscription() {}

    // Constructor to create a subscription with all main fields except id and user
    public Subscription(String name, double price, String duration, String paymentFrequency) {
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.paymentFrequency = paymentFrequency;
    }

    // Getters and setters allow other parts of the program to access and change the fields

    // Get the id of the subscription
    public Long getId() { return id; }
    // Set the id of the subscription
    public void setId(Long id) { this.id = id; }

    // Get the name of the subscription
    public String getName() { return name; }
    // Set the name of the subscription
    public void setName(String name) { this.name = name; }

    // Get the price of the subscription
    public double getPrice() { return price; }
    // Set the price of the subscription
    public void setPrice(double price) { this.price = price; }

    // Get the duration of the subscription
    public String getDuration() { return duration; }
    // Set the duration of the subscription
    public void setDuration(String duration) { this.duration = duration; }

    // Get the payment frequency of the subscription
    public String getPaymentFrequency() { return paymentFrequency; }
    // Set the payment frequency of the subscription
    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    // Get the user who owns this subscription
    public User getUser() { return user; }
    // Set the user who owns this subscription
    public void setUser(User user) { this.user = user; }
}

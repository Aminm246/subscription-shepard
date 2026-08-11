package edu.metro.subscriptionshepard;

// Import Spring's dependency injection and service annotations
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// Import annotation to manage database transactions automatically
import org.springframework.transaction.annotation.Transactional;
// Import List and Optional for handling collections and possible null values
import java.util.List;
import java.util.Optional;

// Mark this class as a service so Spring knows to manage it
@Service
// Make all public methods run inside a database transaction
@Transactional
public class SubscriptionService {

    // Inject the SubscriptionRepository so we can use it for database operations
    @Autowired
    private SubscriptionRepository repository;

    // Constructor for dependency injection
    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    // Save a new subscription to the database
    public void create(Subscription sub) {
        repository.save(sub);
    }

    // Update an existing subscription in the database
    public void update(Subscription sub) {
        repository.save(sub);
    }

    // Delete a subscription from the database by its id
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // Retrieve a subscription by its id
    // Returns an Optional which may or may not contain a Subscription
    public Optional<Subscription> retrieve(Long id) {
        return repository.findById(id);
    }

    // Retrieve all subscriptions from the database
    public List<Subscription> retrieveAll() {
        return repository.findAll();
    }

    // Retrieve all subscriptions that belong to a specific user
    // Calls the repository method that finds subscriptions by user
    public List<Subscription> retrieveByUser(User user) {
        return repository.findByUser(user);
    }
}

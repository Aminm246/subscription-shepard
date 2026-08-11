package edu.metro.subscriptionshepard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /**
     * Finds all subscriptions belonging to a specific user.
     * @param user The user to filter by
     * @return List of subscriptions linked to this user
     */
    List<Subscription> findByUser(User user);
}

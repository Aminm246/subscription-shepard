package edu.metro.subscriptionshepard;

// Import JpaRepository to provide basic CRUD operations for User objects
import org.springframework.data.jpa.repository.JpaRepository;
// Import Optional to handle cases where a user may or may not be found
import java.util.Optional;

/**
 * This interface allows you to work with the users table in the database
 * It extends JpaRepository so you get methods like save findAll and delete for free
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username
     * Used by the authentication system to look up users during login
     * Returns an Optional which means it may or may not contain a User
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username already exists in the database
     * Used during registration to prevent duplicate usernames
     * Returns true if the username exists and false otherwise
     */
    boolean existsByUsername(String username);
}

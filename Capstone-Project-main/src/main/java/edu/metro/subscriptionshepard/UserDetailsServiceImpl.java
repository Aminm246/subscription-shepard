package edu.metro.subscriptionshepard;

// Import needed Spring Security and Java classes for user authentication
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

/**
 * This class is a custom implementation of Spring Security's UserDetailsService
 * It connects your User entity to Spring Security's authentication system
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // UserRepository is used to access user data from the database
    private final UserRepository userRepository;

    // Constructor with @Autowired tells Spring to inject the UserRepository automatically
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called by Spring Security when a user tries to log in
     * It loads user details from the database using the provided username
     *
     * @param username The username entered by the user during login
     * @return UserDetails object containing username, encrypted password, and user roles
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look for a user in the database with the given username
        User user = userRepository.findByUsername(username)
                // If not found, throw an exception so login fails
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with username '" + username + "' not found"
                ));

        // Return a Spring Security UserDetails object
        // This includes the username, encrypted password, and a list of authorities or roles
        // Every user is given the ROLE_USER authority
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

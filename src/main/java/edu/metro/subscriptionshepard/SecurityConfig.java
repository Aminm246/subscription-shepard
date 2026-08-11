package edu.metro.subscriptionshepard;

// Import necessary Spring Security and configuration classes
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// Mark this class as a configuration for Spring
@Configuration
// Enable Spring Security for the application
@EnableWebSecurity
public class SecurityConfig {

    // Define a bean that provides a password encoder using BCrypt for hashing passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is a strong hashing algorithm for storing passwords securely
        return new BCryptPasswordEncoder();
    }

    // Define the main security filter chain bean
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection only for the H2 database console so it works in the browser
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**"))
                )

                // Set up which URLs are allowed without logging in and which require authentication
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the H2 console, login page, registration page and static CSS or JS files
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/h2-console/**"),
                                AntPathRequestMatcher.antMatcher("/login"),
                                AntPathRequestMatcher.antMatcher("/register"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/js/**")
                        ).permitAll()
                        // Any other request requires the user to be logged in
                        .anyRequest().authenticated()
                )

                // Configure the login form
                .formLogin(form -> form
                        // Use the login page at the /login URL
                        .loginPage("/login")
                        // After successful login go to the dashboard page
                        .defaultSuccessUrl("/dashboard")
                        // If login fails go back to the login page with an error message
                        .failureUrl("/login?error=true")
                        // Allow everyone to see the login page
                        .permitAll()
                )

                // Configure logout behavior
                .logout(logout -> logout
                        // After logout go to the login page with a logout message
                        .logoutSuccessUrl("/login?logout")
                        // Allow everyone to access the logout URL
                        .permitAll()
                        // Invalidate the session so the user is fully logged out
                        .invalidateHttpSession(true)
                        // Delete the session cookie for extra security
                        .deleteCookies("JSESSIONID")
                )

                // Allow the H2 database console to open in a frame in the browser
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        // Return the built security filter chain to Spring
        return http.build();
    }
}

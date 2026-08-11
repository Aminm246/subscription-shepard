package edu.metro.subscriptionshepard;

// Import needed Spring and Java classes for web and security features
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// This class handles user registration and login pages
@Controller
public class AuthController {

    // These are the tools we need for user storage and password encoding
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring will automatically give us these dependencies when creating the controller
    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show the registration form when someone goes to /register in the browser
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Add a new empty user object to the page so the form can use it
        model.addAttribute("user", new User());
        // Show the register.html page
        return "register";
    }

    // Handle the form submission when someone fills out registration and clicks submit
    @PostMapping("/register")
    public String processRegistration(
            @ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes // Used to show messages after redirect
    ) {
        // Check if the username is already taken in the database
        if (userRepository.existsByUsername(user.getUsername())) {
            // If taken, show an error message and send the user back to the registration page
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Username '" + user.getUsername() + "' is already taken!"
            );
            return "redirect:/register";
        }

        // If the username is available it encrypts the password for security
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save the new user in the database
        userRepository.save(user);

        // Show a success message so the user knows they can now log in
        redirectAttributes.addFlashAttribute(
                "success",
                "Account created successfully! You can now login."
        );
        // Clear the form fields for the next registration
        redirectAttributes.addFlashAttribute("user", new User());

        // Stay on the registration page after successful registration
        return "redirect:/register";
    }

    // Show the login form when someone goes to /login in the browser
    @GetMapping("/login")
    public String showLoginForm() {
        // Show the login.html page
        return "login";
    }
}

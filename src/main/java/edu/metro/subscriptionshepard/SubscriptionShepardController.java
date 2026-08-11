package edu.metro.subscriptionshepard;

// Import needed Spring and Java classes for controllers, security, and data
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Mark this class as a Spring MVC controller
@Controller
public class SubscriptionShepardController {

    // Inject the SubscriptionService to handle business logic for subscriptions
    @Autowired
    private SubscriptionService subService;

    // Inject the UserRepository to access user data from the database
    @Autowired
    private UserRepository userRepository;

    // Handle requests for the dashboard page at the root URL or /dashboard
    @RequestMapping(value={"","/","/dashboard"})
    public String dashboard(Model model){
        // Get the currently logged-in user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        // If no user is logged in, redirect to the login page
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get only the subscriptions that belong to the current user
        List<Subscription> subscriptions = subService.retrieveByUser(currentUser);

        // Calculate the total monthly cost for all the user's subscriptions
        double totalMonthly = 0.0;
        for (Subscription sub : subscriptions) {
            double monthlyPrice = sub.getPrice();
            if ("Yearly".equals(sub.getPaymentFrequency())) {
                monthlyPrice = sub.getPrice() / 12;
            } else if ("Quarterly".equals(sub.getPaymentFrequency())) {
                monthlyPrice = sub.getPrice() / 3;
            } else if ("Weekly".equals(sub.getPaymentFrequency())) {
                monthlyPrice = sub.getPrice() * 4.33;
            }
            totalMonthly += monthlyPrice;
        }

        // Add the subscription list, total monthly cost, and username to the model for the dashboard page
        model.addAttribute("subscription", new Subscription());
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("totalMonthly", totalMonthly);
        model.addAttribute("username", currentUser.getUsername());
        return "dashboard";
    }

    // Show the form to add a new subscription
    @GetMapping("/addSpending")
    public String showAddForm(Model model) {
        // Add a new empty subscription object to the model for the form
        model.addAttribute("subscription", new Subscription());
        return "addSpending";
    }

    // Handle the form submission to add a new subscription
    @PostMapping("/addSpending")
    public String addSpending(@ModelAttribute Subscription sub){
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);

        // Set the user for the new subscription so it is linked to the current user
        sub.setUser(currentUser);

        // Save the new subscription to the database
        subService.create(sub);
        return "redirect:/dashboard";
    }

    // Show the form to edit a subscription by its id
    @GetMapping("/retrieve/{id}")
    public String retrieveAll(@PathVariable Long id, Model model){
        // Find the subscription by id and add it to the model
        model.addAttribute("subscription", subService.retrieve(id).orElse(new Subscription()));
        return "/subscription";
    }

    // Delete a subscription by its id and go back to the dashboard
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id){
        subService.delete(id);
        return "redirect:/dashboard";
    }

    // Handle the form submission to update an existing subscription
    @PostMapping("/update")
    public String update(@ModelAttribute Subscription sub){
        // Get the currently logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);

        // Set the user for the updated subscription
        sub.setUser(currentUser);

        // Update the subscription in the database
        subService.update(sub);
        return "redirect:/dashboard";
    }
}

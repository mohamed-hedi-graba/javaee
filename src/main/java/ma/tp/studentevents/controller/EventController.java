package ma.tp.studentevents.controller;

import jakarta.servlet.http.HttpSession;
import ma.tp.studentevents.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/events")
    public String showEvents(@RequestParam(value = "search", required = false) String search, 
                             HttpSession session, Model model) {
        if (session.getAttribute("loggedUser") == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("events", eventService.searchEvents(search));
        model.addAttribute("search", search);
        return "events";
    }
}

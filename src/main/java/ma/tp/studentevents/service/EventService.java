package ma.tp.studentevents.service;

import ma.tp.studentevents.dao.EventDao;
import ma.tp.studentevents.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class EventService {

    @Autowired
    private EventDao eventDao;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        if (eventDao.findAll().isEmpty()) {
            eventDao.save(new Event("Atelier Java Spring", "Découverte de Spring 6 et Hibernate", new Date()));
            eventDao.save(new Event("Conférence IA", "L'avenir de l'Intelligence Artificielle", new Date()));
            eventDao.save(new Event("Soirée d'intégration", "Rencontre entre étudiants", new Date()));
        }
    }

    public List<Event> searchEvents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return eventDao.findAll();
        }
        return eventDao.search(keyword);
    }
}

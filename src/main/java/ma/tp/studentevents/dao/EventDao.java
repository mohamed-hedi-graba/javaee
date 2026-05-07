package ma.tp.studentevents.dao;

import ma.tp.studentevents.model.Event;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventDao {

    @Autowired
    private SessionFactory sessionFactory;

    public void save(Event event) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(event);
    }

    public List<Event> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("from Event", Event.class).list();
    }

    public List<Event> search(String keyword) {
        Session session = sessionFactory.getCurrentSession();
        Query<Event> query = session.createQuery("from Event where lower(title) like :kw or lower(description) like :kw", Event.class);
        query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        return query.list();
    }
}

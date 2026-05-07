package ma.tp.studentevents.service;

import ma.tp.studentevents.dao.UserDao;
import ma.tp.studentevents.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserDao userDao;

    public boolean register(User user) {
        if (userDao.findByEmail(user.getEmail()) != null) {
            return false; // Email already exists
        }
        // Hash password
        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        userDao.save(user);
        return true;
    }

    public User login(String email, String password) {
        User user = userDao.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}

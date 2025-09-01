package org.example.service;

import org.example.model.User;
import org.example.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User registerUser(String mail, String password, String phone) {
        User user = new User();
        user.setMail(mail);
        user.setPassword(password); // Hash in production!
        user.setPhone(phone);
        user.setRole("TENANT");
        return userRepository.save(user);
    }
}

package ru.practicum.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirst1ByName(String name);
}
package io.project.bankbot.model;

import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User,Long> {

    User findByUsername(String username);

    User findByChatId(Long chatId);
}

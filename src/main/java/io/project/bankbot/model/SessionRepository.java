package io.project.bankbot.model;

import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<Session, Long> {

    Session findBySessionChatId(Long chatId);


}

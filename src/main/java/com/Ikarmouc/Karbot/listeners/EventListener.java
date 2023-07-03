package com.Ikarmouc.Karbot.listeners;
import discord4j.core.*;
import discord4j.core.event.domain.Event;
import reactor.core.*;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event> {
    Class<T> getEventTyoe();

    Mono<Void> execute(T event);

    default Mono<Void> handleError(final Throwable error){
        error.printStackTrace();
        return Mono.empty();
    }
}

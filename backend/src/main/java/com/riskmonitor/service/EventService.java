package com.riskmonitor.service;

import com.riskmonitor.dto.EventDTO;
import com.riskmonitor.model.Event;
import com.riskmonitor.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public List<EventDTO> getRecentEvents() {
        // Fetch all non-weather_update events, then find the latest for each (supplier, type) combo
        Map<String, Event> latestEventsMap = eventRepository.findAll().stream()
            .filter(e -> !"weather_update".equals(e.getType()))
            .collect(Collectors.toMap(
                e -> e.getSupplier().getId() + ":" + e.getType(),
                e -> e,
                (existing, replacement) -> existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
            ));

        return latestEventsMap.values().stream()
            .sorted(Comparator.comparing(Event::getTimestamp).reversed())
            .limit(20)
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private EventDTO convertToDTO(Event event) {
        return new EventDTO(
            event.getId(),
            event.getSupplier().getName(),
            event.getTimestamp(),
            event.getType(),
            event.getPayload()
        );
    }
}

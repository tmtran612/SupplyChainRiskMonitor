package com.riskmonitor.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.riskmonitor.dto.EventDTO;
import com.riskmonitor.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/recent")
    public List<EventDTO> getRecentEvents() {
        return eventService.getRecentEvents();
    }
}
package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.persistence.firebase.FirebaseRealtimeDatabaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseHealthController {

    private final FirebaseRealtimeDatabaseRepository firebaseRepository;

    public FirebaseHealthController(FirebaseRealtimeDatabaseRepository firebaseRepository) {
        this.firebaseRepository = firebaseRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        firebaseRepository.reference("server/health/lastCheck")
                .setValueAsync(Instant.now().toString());

        return ResponseEntity.ok(Map.of(
                "status", "Firebase connection configured",
                "databasePath", "server/health/lastCheck"
        ));
    }
}

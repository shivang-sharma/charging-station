package com.evcharginstation.backend.controller;

import com.evcharginstation.backend.persistency.jpa.entity.EVStation;
import com.evcharginstation.backend.service.EVStationService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/stations")
public class EVStationController {
    private final EVStationService evStationService;
    public EVStationController(EVStationService evStationService) {
        this.evStationService = evStationService;
    }

    @GetMapping
    public List<EVStation> getEVStations(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "param", required = false) String param
    ) {
        if (Optional.ofNullable(limit).isPresent()) {
            return this.evStationService.getLimitedEVStations(limit);
        } else if (Optional.ofNullable(sort).isPresent() && !Optional.ofNullable(param).isPresent()) {
            return this.evStationService.getSortedEVStations(sort, "default");
        } else if (Optional.ofNullable(sort).isPresent()) {
            return this.evStationService.getSortedEVStations(sort, param);
        }
        return this.evStationService.getEVStations();
    }

    @GetMapping(value = "/images/{image}")
    public ResponseEntity<Resource> getImage(@PathVariable("image") String image) {
        try {
            final ByteArrayResource imageStream = this.evStationService.getImage(image);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(imageStream.contentLength())
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                    .body(imageStream);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EVStation> addEVStation(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "price") float price,
            @RequestParam(name = "address") String address,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            if (Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
                EVStation newEVStation  = this.evStationService.addEVStation(image, name, price, address);
                return ResponseEntity.status(HttpStatus.CREATED).body(newEVStation);

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GetMapping(value = "/show/{id}")
    public ResponseEntity<EVStation> getStation(@PathVariable Long id) {
        Optional<EVStation> evStation =  this.evStationService.getEVStation(id);
        return evStation.map(station -> ResponseEntity.status(HttpStatus.FOUND).body(station)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}/edit")
    public ResponseEntity<EVStation> updateStation(
            @PathVariable("id") Long id,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "price") float price,
            @RequestParam(name = "address") String address,
            @RequestParam(name = "image", required = false) MultipartFile image
    ) {
        try {
            if (Optional.ofNullable(image).isPresent() && Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(this.evStationService.updateEVStation(id, name, price, address, image));
            } else {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(this.evStationService.updateEVStation(id, name, price, address));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<String> deleteEVStation(@PathVariable("id") Long id) {
        try {
            this.evStationService.deleteEVStation(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

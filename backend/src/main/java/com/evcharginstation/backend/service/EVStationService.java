package com.evcharginstation.backend.service;

import com.evcharginstation.backend.persistency.jpa.entity.EVStation;
import com.evcharginstation.backend.persistency.jpa.repository.EVStationRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EVStationService {
    private final EVStationRepository evStationRepository;
    private final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/images/";

    public EVStationService(EVStationRepository evStationRepository) {
        this.evStationRepository = evStationRepository;
    }

    public List<EVStation> getEVStations() {
        return new ArrayList<>(this.evStationRepository.findAll());
    }

    public Optional<EVStation> getEVStation(Long id) {
        return this.evStationRepository.findById(id);
    }

    public EVStation addEVStation(MultipartFile image, String name, float price, String address) {
        try {
            String imageName = uploadImage(image);
            return this.updateOrSave(null, name, price, address, imageName);
        } catch (Exception e) {
            throw new RuntimeException("Could not add new station");
        }
    }

    public ByteArrayResource getImage(String image) throws IOException {
        Path requestedImagePath = Paths.get(UPLOAD_DIRECTORY+image+".jpeg");
        if (Files.exists(requestedImagePath)) {
            return new ByteArrayResource(Files.readAllBytes(requestedImagePath));
        }
        throw new RuntimeException("Image does not exists");
    }

    public EVStation updateEVStation(Long id, String name, float price, String address, MultipartFile image) {
        try {
            String imageName = uploadImage(image);
            return this.updateOrSave(id, name, price, address, imageName);
        } catch (Exception e) {
            throw new RuntimeException("Could not add new station");
        }
    }

    public EVStation updateEVStation(Long id, String name, float price, String address) {
        try {
            String imageName = this.evStationRepository.findById(id).map(EVStation::getImage).orElse("");
            return this.updateOrSave(id, name, price, address, imageName);
        } catch (Exception e) {
            throw new RuntimeException("Could not add new station");
        }
    }

    public void deleteEVStation(Long id) throws IOException {
        if (this.evStationRepository.findById(id).isPresent()) {
            String imageName = this.evStationRepository.findById(id).get().getImage();
            imageName = imageName.split("/")[imageName.split("/").length-1];
            Files.delete(Paths.get(UPLOAD_DIRECTORY, imageName + ".jpeg"));
            this.evStationRepository.deleteById(id);
        } else {
            throw new RuntimeException("EVStation does not exits");
        }
    }

    public List<EVStation> getLimitedEVStations(int limit) {
        Pageable limited = PageRequest.ofSize(limit);
        return this.evStationRepository.findAllByOrderByName(limited);
    }

    public List<EVStation> getSortedEVStations(String sort, String param) {
        Sort sortQuery;
        switch(param.toUpperCase()) {
            case "STATION_NAME":
                if (sort.equalsIgnoreCase( "asc")) {
                    sortQuery = Sort.by("name").ascending();
                } else  {
                    sortQuery = Sort.by("name").descending();
                }
                break;
            case "STATION_PRICING":
                if (sort.equalsIgnoreCase( "asc")) {
                    sortQuery = Sort.by("price").ascending();
                }
                else {
                    sortQuery = Sort.by("price").descending();
                }
                break;
            default: if (sort.equalsIgnoreCase( "asc")) {
                sortQuery = Sort.by("id").ascending();
            }
            else {
                sortQuery = Sort.by("id").descending();
            }
        }
        return this.evStationRepository.findAll(sortQuery);
    }
    public EVStation updateOrSave(Long id, String name, float price, String address, String imageName) {
        Optional<Long> optionalId = Optional.ofNullable(id);
        EVStation newEVStation = new EVStation();
        if (optionalId.isPresent()) {
            newEVStation.setId(id);
        }
        newEVStation.setName(name);
        newEVStation.setPrice(price);
        newEVStation.setAddress(address);
        if (imageName.startsWith("/api/stations/images/")) {
            newEVStation.setImage(imageName);
        } else {
            newEVStation.setImage("/api/stations/images/" + imageName);
        }
        return this.evStationRepository.save(newEVStation);
    }

    private String uploadImage(MultipartFile image) throws IOException{
        String imageName = DigestUtils.md5DigestAsHex(image.getBytes());
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, imageName + ".jpeg");
        Files.write(fileNameAndPath, image.getBytes());
        return imageName;
    }
}

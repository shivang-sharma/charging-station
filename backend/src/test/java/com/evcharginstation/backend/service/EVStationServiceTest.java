package com.evcharginstation.backend.service;

import com.evcharginstation.backend.persistency.jpa.entity.EVStation;
import com.evcharginstation.backend.persistency.jpa.repository.EVStationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class EVStationServiceTest {

    @Mock
    private EVStationRepository evStationRepository;
    @InjectMocks
    private EVStationService evStationService;
    private EVStation evStation1;
    private EVStation evStation2;

    @BeforeEach
    void setUp() {
        evStation1 = new EVStation();
        evStation1.setId(101);
        evStation1.setName("EV Station 1");
        evStation1.setAddress("4100 Jackson Ave Austin Texas 78731");
        evStation1.setPrice(4.65F);
        evStation1.setImage("/api/stations/images/023d2e8c4029412e1532319af131e6d0");
        evStation2 = new EVStation();
        evStation2.setId(102);
        evStation2.setName("EV Station 2");
        evStation2.setAddress("3rd Avenue Austin Texas 78731");
        evStation2.setPrice(7.65F);
        evStation2.setImage("/api/stations/images/023d2e8c4029412e1532319af131e6d0");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getEVStations() {
        List<EVStation> evStationList = new ArrayList<>();
        evStationList.add(evStation1);
        when(evStationRepository.findAll()).thenReturn(evStationList);
        List<EVStation> evStations = evStationService.getEVStations();
        assertEquals(evStations.size(), 1);
    }

    @Test
    void getEVStation() {
        when(evStationRepository.findById(evStation1.getId())).thenReturn(Optional.of(evStation1));
        Optional<EVStation> evStationResponse = evStationService.getEVStation(evStation1.getId());
        assertTrue(evStationResponse.isPresent());
        assertEquals(evStationResponse.get().getId(), evStation1.getId());
        evStationResponse = evStationService.getEVStation(0L);
        assertTrue(evStationResponse.isEmpty());
    }

    @Test
    void getImage() {
        try {
            ByteArrayResource byteArrayResource = evStationService.getImage("023d2e8c4029412e1532319af131e6d0");
            assertTrue(byteArrayResource.exists());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deleteEVStation() {
        when(evStationRepository.findById(evStation1.getId())).thenReturn(Optional.ofNullable(evStation1));
        doAnswer(invocationOnMock -> {
            assertEquals(evStation1.getId(), (Long) invocationOnMock.getArgument(0));
            return null;
        }).when(evStationRepository).deleteById(evStation1.getId());
        doThrow(RuntimeException.class).when(evStationRepository).deleteById(0L);
    }

    @Test
    void getLimitedEVStations() {
        List<EVStation> evStationList = new ArrayList<>();
        evStationList.add(evStation1);
        Pageable limited = PageRequest.ofSize(1);
        when(evStationRepository.findAllByOrderByName(limited)).thenReturn(evStationList);
        List<EVStation> limitedList = evStationService.getLimitedEVStations(1);
        assertEquals(limitedList.size(), 1);
    }

    @Test
    void getSortedEVStationsByStationName() {
        Sort ascendingSortQuery = Sort.by("name").ascending();
        Sort descendingSortQuery = Sort.by("name").descending();
        List<EVStation> ascendingEVStations = new ArrayList<>();
        ascendingEVStations.add(evStation2);
        ascendingEVStations.add(evStation1);
        List<EVStation> descendingEVStations = new ArrayList<>();
        descendingEVStations.add(evStation1);
        descendingEVStations.add(evStation2);
        when(evStationRepository.findAll(ascendingSortQuery)).thenReturn(ascendingEVStations);
        when(evStationRepository.findAll(descendingSortQuery)).thenReturn(descendingEVStations);
        List<EVStation> responseList = evStationService.getSortedEVStations("asc", "station_name");
        assertEquals(responseList.get(0).getId(), 102);
        assertEquals(responseList.get(1).getId(), 101);
        responseList = evStationService.getSortedEVStations("desc", "station_name");
        assertEquals(responseList.get(0).getId(), 101);
        assertEquals(responseList.get(1).getId(), 102);
    }
    @Test
    void getSortedEVStationsByStationPrice() {
        Sort ascendingSortQuery = Sort.by("price").ascending();
        Sort descendingSortQuery = Sort.by("price").descending();
        List<EVStation> ascendingEVStations = new ArrayList<>();
        ascendingEVStations.add(evStation1);
        ascendingEVStations.add(evStation2);
        List<EVStation> descendingEVStations = new ArrayList<>();
        descendingEVStations.add(evStation2);
        descendingEVStations.add(evStation1);
        when(evStationRepository.findAll(ascendingSortQuery)).thenReturn(ascendingEVStations);
        when(evStationRepository.findAll(descendingSortQuery)).thenReturn(descendingEVStations);
        List<EVStation> responseList = evStationService.getSortedEVStations("asc", "station_pricing");
        assertEquals(responseList.get(0).getId(), 101);
        assertEquals(responseList.get(1).getId(), 102);
        assertTrue(responseList.get(0).getPrice() < responseList.get(1).getPrice());
        responseList = evStationService.getSortedEVStations("desc", "station_pricing");
        assertEquals(responseList.get(0).getId(), 102);
        assertEquals(responseList.get(1).getId(), 101);
        assertTrue(responseList.get(0).getPrice() > responseList.get(1).getPrice());
    }
    @Test
    void getSortedEVStations() {
        Sort ascendingSortQuery = Sort.by("id").ascending();
        Sort descendingSortQuery = Sort.by("id").descending();
        List<EVStation> ascendingEVStations = new ArrayList<>();
        ascendingEVStations.add(evStation1);
        ascendingEVStations.add(evStation2);
        List<EVStation> descendingEVStations = new ArrayList<>();
        descendingEVStations.add(evStation2);
        descendingEVStations.add(evStation1);
        when(evStationRepository.findAll(ascendingSortQuery)).thenReturn(ascendingEVStations);
        when(evStationRepository.findAll(descendingSortQuery)).thenReturn(descendingEVStations);
        List<EVStation> responseList = evStationService.getSortedEVStations("asc", "id");
        assertEquals(responseList.get(0).getId(), 101);
        assertEquals(responseList.get(1).getId(), 102);
        responseList = evStationService.getSortedEVStations("desc", "id");
        assertEquals(responseList.get(0).getId(), 102);
        assertEquals(responseList.get(1).getId(), 101);
    }
}
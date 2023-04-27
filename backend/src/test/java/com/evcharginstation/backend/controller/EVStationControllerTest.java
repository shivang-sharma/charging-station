package com.evcharginstation.backend.controller;

import com.evcharginstation.backend.persistency.jpa.entity.EVStation;
import com.evcharginstation.backend.service.EVStationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EVStationController.class)
class EVStationControllerTest {
    @MockBean
    private EVStationService evStationService;
    @Autowired
    private MockMvc mockMvc;
    private EVStation evStation1;
    private MockMultipartFile image;
    @BeforeEach
    void setUp() {
        evStation1 = new EVStation();
        evStation1.setId(1);
        evStation1.setAddress("Austin Texas");
        evStation1.setName("EV Charging Point");
        evStation1.setPrice(5.87F);
        evStation1.setImage("api/stations/images/023d2e8c4029412e1532319af131e6d0");
        image = new MockMultipartFile(
                "image",
                "station_image.png",
                String.valueOf(MediaType.IMAGE_PNG),
                "Dummy Image Data".getBytes());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getEVStations() throws Exception {
        List<EVStation> stations = new ArrayList<>();
        stations.add(evStation1);
        when(evStationService.getEVStations()).thenReturn(stations);
        this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/stations"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    @Test
    void getImage() throws Exception {
        ByteArrayResource image = new ByteArrayResource("Dummy Image Data".getBytes());
        when(evStationService.getImage("023d2e8c4029412e1532319af131e6d0")).thenReturn(image);
        this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/stations/images/023d2e8c4029412e1532319af131e6d0")
            .contentType(MediaType.IMAGE_PNG)
            .accept(MediaType.IMAGE_PNG))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(header().exists("Content-Length"))
            .andExpect(header().longValue("Content-Length", image.contentLength()));
    }

    @Test
    void addEVStation() throws Exception {
        when(evStationService.addEVStation(
                image,
                evStation1.getName(),
                evStation1.getPrice(),
                evStation1.getAddress()
        )).thenReturn(evStation1);
        this.mockMvc.perform(MockMvcRequestBuilders
                .multipart("/api/stations")
                .file(image)
                .param("name", evStation1.getName())
                .param("price", String.valueOf(evStation1.getPrice()))
                .param("address", evStation1.getAddress())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getStation() throws Exception {
        when(evStationService.getEVStation(evStation1.getId())).thenReturn(Optional.ofNullable(evStation1));
        this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/stations/show/{id}", evStation1.getId()))
            .andDo(print())
            .andExpect(status().isFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isMap())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber());
    }

    @Test
    void updateStation() throws Exception {
        when(evStationService.updateEVStation(
                evStation1.getId(),
                evStation1.getName(),
                evStation1.getPrice(),
                evStation1.getAddress())).thenReturn(evStation1);
        when(evStationService.updateEVStation(
                evStation1.getId(),
                evStation1.getName(),
                evStation1.getPrice(),
                evStation1.getAddress(),
                image)).thenReturn(evStation1);
        this.mockMvc.perform(MockMvcRequestBuilders
                .put("/api/stations/{id}/edit", evStation1.getId())
                .param("name", evStation1.getName())
                .param("price", String.valueOf(evStation1.getPrice()))
                .param("address", evStation1.getAddress()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());
        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/stations/{id}/edit", evStation1.getId());
        builder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });
        this.mockMvc.perform(builder
                        .file(image)
                        .param("name", evStation1.getName())
                        .param("price", String.valueOf(evStation1.getPrice()))
                        .param("address", evStation1.getAddress()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

    }

    @Test
    void deleteEVStation() throws Exception {
        doAnswer(invocationOnMock -> {
            assertEquals(evStation1.getId(), (Long) invocationOnMock.getArgument(0));
            return null;
        }).when(evStationService).deleteEVStation(evStation1.getId());
        this.mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/stations/delete/{id}", evStation1.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getLimitedEVStations() throws Exception {
        List<EVStation> stations = new ArrayList<>();
        stations.add(evStation1);
        when(evStationService.getLimitedEVStations(1)).thenReturn(stations);
        this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/stations?limit=1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").isNumber());
    }

    @Test
    void getSortedEVStations() throws Exception {
        List<EVStation> stations = new ArrayList<>();
        stations.add(evStation1);
        EVStation evStation2 = new EVStation();
        evStation2.setId(2);
        evStation2.setAddress("Austin Texas");
        evStation2.setName("EV Charging Point");
        evStation2.setPrice(7.87F);
        evStation2.setImage("api/stations/images/023d2e8c4029412e1532319af131e6d0");
        stations.add(evStation2);
        when(evStationService.getSortedEVStations("asc", "station_name")).thenReturn(stations);
        this.mockMvc.perform(MockMvcRequestBuilders
            .get("/api/stations?sort=asc&param=station_name"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }
}
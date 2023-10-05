package com.heartsignal.dev.service.domain;


import com.heartsignal.dev.domain.Bar;
import com.heartsignal.dev.repository.BarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarService {
    private final BarRepository barRepository;
    public List<String> findLocations(){
        return barRepository.findLocation();
    }
    public List<Bar> findBarsInLocation(String location){
        return barRepository.findByLocation(location);
    }

}

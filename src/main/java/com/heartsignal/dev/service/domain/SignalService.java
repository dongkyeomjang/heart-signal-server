package com.heartsignal.dev.service.domain;

import com.heartsignal.dev.domain.Signal;
import com.heartsignal.dev.domain.Team;
import com.heartsignal.dev.domain.User;
import com.heartsignal.dev.repository.SignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalService {
    private final SignalRepository signalRepository;
    @Transactional
    public void saveSignal(Team sendTeam, Team receivedTeam){
        signalRepository.save(
                Signal.builder()
                        .sender(sendTeam)
                        .receiver(receivedTeam)
                        .build());
        log.info("시그널 저장 성공");
    }
    public List<Signal> findSendingSignal(Team myTeam){
        return signalRepository.findBySender(myTeam);
    }
    public List<Signal> findReceivedSignal(Team myTeam){
        return signalRepository.findByReceiver(myTeam);
    }
}

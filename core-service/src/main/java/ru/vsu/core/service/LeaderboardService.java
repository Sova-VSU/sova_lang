package ru.vsu.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.core.model.dto.LeaderboardEntry;
import ru.vsu.core.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    public List<LeaderboardEntry> getLeaderboard() {
        List<LeaderboardEntry> entries = userRepository.findLeaderboard();
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return entries;
    }
}

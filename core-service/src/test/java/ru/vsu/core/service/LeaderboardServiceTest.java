package ru.vsu.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.core.model.dto.LeaderboardEntry;
import ru.vsu.core.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private LeaderboardService leaderboardService;

    @Test
    void getLeaderboard_returnsRankedList() {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().id("u1").name("Alice").completedScenariosCount(5).totalXp(500).build(),
                LeaderboardEntry.builder().id("u2").name("Bob").completedScenariosCount(3).totalXp(300).build(),
                LeaderboardEntry.builder().id("u3").name("Carol").completedScenariosCount(1).totalXp(100).build()
        );
        when(userRepository.findLeaderboard()).thenReturn(entries);

        List<LeaderboardEntry> result = leaderboardService.getLeaderboard();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(1).getRank()).isEqualTo(2);
        assertThat(result.get(2).getRank()).isEqualTo(3);
    }

    @Test
    void getLeaderboard_emptyList_returnsEmpty() {
        when(userRepository.findLeaderboard()).thenReturn(List.of());

        List<LeaderboardEntry> result = leaderboardService.getLeaderboard();

        assertThat(result).isEmpty();
    }

    @Test
    void getLeaderboard_singleEntry_rankIsOne() {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().id("u1").name("Alice").completedScenariosCount(10).totalXp(1000).build()
        );
        when(userRepository.findLeaderboard()).thenReturn(entries);

        List<LeaderboardEntry> result = leaderboardService.getLeaderboard();

        assertThat(result.get(0).getRank()).isEqualTo(1);
    }
}

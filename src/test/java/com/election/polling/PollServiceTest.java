package com.election.polling;

import com.election.polling.dto.PollRequest;
import com.election.polling.dto.PollResponse;
import com.election.polling.dto.VoteRequest;
import com.election.polling.entity.Option;
import com.election.polling.entity.Poll;
import com.election.polling.entity.Vote;
import com.election.polling.exception.VotingException;
import com.election.polling.repository.OptionRepository;
import com.election.polling.repository.PollRepository;
import com.election.polling.repository.VoteRepository;
import com.election.polling.service.PollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollServiceTest {

    @Mock
    private PollRepository pollRepo;
    @Mock
    private OptionRepository optionRepo;
    @Mock
    private VoteRepository voteRepo;

    @InjectMocks
    private PollServiceImpl pollService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPoll_shouldReturnPollResponse() {
        PollRequest request = new PollRequest();
        request.setQuestion("Best language?");
        request.setExpiryDateTime(LocalDateTime.now().plusDays(1));
        request.setOptions(List.of("Java", "Python"));

        // Mock save
        when(pollRepo.save(any(Poll.class))).thenAnswer(invocation -> {
            Poll p = invocation.getArgument(0);
            p.setId(1L);
            p.getOptions().forEach(opt -> opt.setId(new Random().nextLong()));
            return p;
        });

        PollResponse response = pollService.generatePoll(request);

        assertNotNull(response);
        assertEquals("Best language?", response.getQuestion());
        assertEquals(2, response.getOptions().size());
    }

    @Test
    void vote_shouldSucceed() {
        Long pollId = 1L;
        String userId = "user123";
        Long optionId = 10L;

        Poll poll = new Poll();
        poll.setId(pollId);
        poll.setExpiryDateTime(LocalDateTime.now().plusHours(2));

        Option option = new Option();
        option.setId(optionId);
        option.setPoll(poll);
        option.setVoteCount(0);

        when(pollRepo.findById(pollId)).thenReturn(Optional.of(poll));
        when(voteRepo.existsByPollIdAndUserId(pollId, userId)).thenReturn(false);
        when(optionRepo.findById(optionId)).thenReturn(Optional.of(option));

        VoteRequest request = new VoteRequest();
        request.setUserId(userId);
        request.setOptionId(optionId);

        pollService.vote(pollId, request);

        assertEquals(1, option.getVoteCount());
        verify(voteRepo, times(1)).save(any(Vote.class));
        verify(optionRepo, times(1)).save(option);
    }

    @Test
    void vote_shouldFail_ifPollExpired() {
        Long pollId = 1L;
        Poll poll = new Poll();
        poll.setId(pollId);
        poll.setExpiryDateTime(LocalDateTime.now().minusMinutes(10));

        when(pollRepo.findById(pollId)).thenReturn(Optional.of(poll));

        VoteRequest request = new VoteRequest();
        request.setUserId("user1");
        request.setOptionId(99L);

        assertThrows(VotingException.class, () -> pollService.vote(pollId, request));
    }

    @Test
    void vote_shouldFail_ifAlreadyVoted() {
        Long pollId = 1L;
        String userId = "user123";

        Poll poll = new Poll();
        poll.setId(pollId);
        poll.setExpiryDateTime(LocalDateTime.now().plusDays(1));

        when(pollRepo.findById(pollId)).thenReturn(Optional.of(poll));
        when(voteRepo.existsByPollIdAndUserId(pollId, userId)).thenReturn(true);

        VoteRequest request = new VoteRequest();
        request.setUserId(userId);
        request.setOptionId(99L);

        assertThrows(VotingException.class, () -> pollService.vote(pollId, request));
    }

    @Test
    void getPollResults_shouldReturnPoll() {
        Long pollId = 1L;

        Option option1 = new Option();
        option1.setId(1L);
        option1.setDesc("Java");
        option1.setVoteCount(5);

        Option option2 = new Option();
        option2.setId(2L);
        option2.setDesc("Python");
        option2.setVoteCount(3);

        Poll poll = new Poll();
        poll.setId(pollId);
        poll.setQuestion("Favorite language?");
        poll.setExpiryDateTime(LocalDateTime.now().plusDays(1));
        poll.setOptions(List.of(option1, option2));

        when(pollRepo.findById(pollId)).thenReturn(Optional.of(poll));

        PollResponse response = pollService.getVoteResults(pollId);

        assertEquals("Favorite language?", response.getQuestion());
        assertEquals(2, response.getOptions().size());
    }
}
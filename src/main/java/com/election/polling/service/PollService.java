package com.election.polling.service;

import com.election.polling.dto.PollRequest;
import com.election.polling.dto.PollResponse;
import com.election.polling.dto.VoteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface PollService {

    public PollResponse generatePoll(PollRequest request);

    public void vote(Long pollId, VoteRequest request);

    public PollResponse getVoteResults(Long pollId);

    public Page<PollResponse> getPollList(Pageable pageable);

    public void updatePoll(Long pollId, PollRequest request);

    public void deactivatePoll(Long pollId);

    public List<PollResponse> getVisiblePolls();
}

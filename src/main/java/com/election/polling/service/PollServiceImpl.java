package com.election.polling.service;

import com.election.polling.dto.OptionResponse;
import com.election.polling.dto.PollRequest;
import com.election.polling.dto.PollResponse;
import com.election.polling.dto.VoteRequest;
import com.election.polling.entity.Option;
import com.election.polling.entity.Poll;
import com.election.polling.entity.PollStatus;
import com.election.polling.entity.Vote;
import com.election.polling.exception.PollNotFoundException;
import com.election.polling.exception.VotingException;
import com.election.polling.repository.OptionRepository;
import com.election.polling.repository.PollRepository;
import com.election.polling.repository.VoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollServiceImpl implements PollService{

    @Autowired
    PollRepository pollRepository;

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public PollResponse generatePoll(PollRequest request) {

        if (request.getOptions().size() > 10) throw new VotingException("A poll can have at most 10 options.");

        Poll poll = new Poll();
        poll.setQuestion(request.getQuestion());
        poll.setExpiryDateTime(request.getExpiryDateTime());

        List<Option> options = request.getOptions().stream()
                .map(desc -> {
                    Option opt = new Option();
                    opt.setDesc(desc);
                    opt.setPoll(poll);
                    return opt;
                }).collect(Collectors.toList());

        poll.setOptions(options);

        Poll generatedPoll = pollRepository.save(poll);
        return mapToResponse(generatedPoll);
    }

    @Override
    @Transactional
    public void vote(Long pollId, VoteRequest request) {

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found"));

        if (poll.getStatus() != PollStatus.OPEN) throw new VotingException("Poll is not open");

        if (poll.getExpiryDateTime().isBefore(LocalDateTime.now())) {
            poll.setStatus(PollStatus.EXPIRED);
            pollRepository.save(poll);
            throw new VotingException("Poll has expired");
        }

        if (poll.getExpiryDateTime().isBefore(LocalDateTime.now())) throw new VotingException("Poll has expired");

        if (voteRepository.existsByPollIdAndUserId(pollId, request.getUserId())) throw new VotingException("User has already voted");

        Option option = optionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new VotingException("Voting Option not found"));

        if (!option.getPoll().getId().equals(pollId)) throw new VotingException("Option does not belong to this poll");

        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setOption(option);
        vote.setUserId(request.getUserId());

        option.setVoteCount(option.getVoteCount() + 1);

        voteRepository.save(vote);

        optionRepository.save(option);

        //This pushes the new result to all subscribed clients.
        /*PollResponse updatedResponse = mapToResponse(poll);
        messagingTemplate.convertAndSend("/topic/poll/" + pollId, updatedResponse);
*/
    }

    @Override
    public PollResponse getVoteResults(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found"));

        return mapToResponse(poll);
    }

    @Override
    public Page<PollResponse> getPollList(Pageable pageable) {
        return pollRepository.findByActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    public void updatePoll(Long pollId, PollRequest request) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found with id " + pollId));

        long votes = voteRepository.countByPollId(pollId);
        if (votes > 0) {
            throw new VotingException("Poll cannot be updated because votes have already been cast.");
        }

        // Update question and expiry
        poll.setQuestion(request.getQuestion());
        poll.setExpiryDateTime(request.getExpiryDateTime());

        // Update options:
        // Remove existing ones and add new ones from request
        optionRepository.deleteAll(poll.getOptions());
        List<Option> newOptions = request.getOptions().stream()
                .map(text -> {
                    Option option = new Option();
                    option.setDesc(text);
                    option.setPoll(poll);
                    return option;
                }).collect(Collectors.toList());

        poll.setOptions(newOptions);

        pollRepository.save(poll);
    }

    public void deactivatePoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found"));
        poll.setActive(false);
        pollRepository.save(poll);
    }

    @Override
    public List<PollResponse> getVisiblePolls() {
        return pollRepository.findVisiblePolls().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private PollResponse mapToResponse(Poll poll) {

        int totalVotes = poll.getOptions().stream().mapToInt(Option::getVoteCount).sum();

        List<OptionResponse> optionResponses = poll.getOptions().stream()
                .map(opt -> {
                    double percent = totalVotes > 0 ? (opt.getVoteCount() * 100.0 / totalVotes) : 0;
                    return new OptionResponse(opt.getId(), opt.getDesc(), opt.getVoteCount(), percent);
                })
                .collect(Collectors.toList());

        return new PollResponse(poll.getId(), poll.getQuestion(), poll.getExpiryDateTime(), optionResponses);

    }
}
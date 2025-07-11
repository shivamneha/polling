package com.election.polling.controller;

import com.election.polling.dto.PollRequest;
import com.election.polling.dto.PollResponse;
import com.election.polling.dto.VoteRequest;
import com.election.polling.service.PollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/poll")
public class PollController {

    public final PollService pollService;

    public PollController(PollService pollService){
        this.pollService = pollService;
    }

    // Generate Poll
    @PostMapping
    public ResponseEntity<PollResponse> generatePoll(@RequestBody PollRequest request) {
        PollResponse response = pollService.generatePoll(request);
        return ResponseEntity.created(URI.create("/api/poll/" + response.getId()))
                .body(response);
    }

    // Cast a vote
    @PostMapping("/{pollId}/vote")
    public ResponseEntity<String> vote(@PathVariable Long pollId, @RequestBody VoteRequest request) {
        pollService.vote(pollId, request);
        return ResponseEntity.ok("Vote submitted successfully");
    }

    // Get poll results
    @GetMapping("/{pollId}/results")
    public ResponseEntity<PollResponse> getResults(@PathVariable Long pollId) {
        PollResponse response = pollService.getVoteResults(pollId);
        return ResponseEntity.ok(response);
    }

    // Get all polls in paged format
    @GetMapping("/paged")
    public ResponseEntity<Page<PollResponse>> getPagedPolls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(pollService.getPollList(pageable));
    }

    // Delete selected poll
    @DeleteMapping("/{pollId}")
    public ResponseEntity<String> deactivate(@PathVariable Long pollId) {
        pollService.deactivatePoll(pollId);
        return ResponseEntity.ok("Poll deactivated");
    }

    @GetMapping("/visible")
    public ResponseEntity<List<PollResponse>> getVisiblePolls() {
        return ResponseEntity.ok(pollService.getVisiblePolls());
    }

    @PutMapping("/{pollId}")
    public ResponseEntity<String> updatePoll(
            @PathVariable Long pollId,
            @RequestBody PollRequest pollRequest) {

        pollService.updatePoll(pollId, pollRequest);
        return ResponseEntity.ok("Poll updated successfully.");
    }

}
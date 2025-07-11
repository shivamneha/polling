package com.election.polling.repository;

import com.election.polling.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote,Integer> {


    public Boolean existsByPollIdAndUserId(Long pollId, String userId);

    public Long countByPollId(Long pollId);
}

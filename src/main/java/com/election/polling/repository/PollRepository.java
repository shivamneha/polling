package com.election.polling.repository;

import com.election.polling.entity.Poll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll,Long> {

    Page<Poll> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Poll p WHERE p.active = true AND p.expiryDateTime > CURRENT_TIMESTAMP")
    List<Poll> findVisiblePolls();
}

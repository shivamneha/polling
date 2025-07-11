package com.election.polling.repository;

import com.election.polling.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository  extends JpaRepository<Option,Long> {
}

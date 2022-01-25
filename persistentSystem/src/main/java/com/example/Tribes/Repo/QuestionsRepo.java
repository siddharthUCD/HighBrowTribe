package com.example.Tribes.Repo;

import com.example.Tribes.Model.Questions;
import com.example.Tribes.Model.TribeQuestionDetails;
import org.springframework.data.repository.CrudRepository;

/**
 * Questions Repository for Questions Table
 */
public interface QuestionsRepo extends CrudRepository<Questions,Long> {
}

package edu.escuelaing.co.leotankcicos.repository;

import edu.escuelaing.co.leotankcicos.model.Board;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends MongoRepository<Board, String> {
}
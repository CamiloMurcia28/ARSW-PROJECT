package edu.escuelaing.co.leotankcicos.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import edu.escuelaing.co.leotankcicos.model.Tank;

@Repository
public interface TankRepository extends MongoRepository<Tank, String> {
}
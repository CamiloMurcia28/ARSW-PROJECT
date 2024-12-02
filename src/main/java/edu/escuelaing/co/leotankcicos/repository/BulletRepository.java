package edu.escuelaing.co.leotankcicos.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import edu.escuelaing.co.leotankcicos.model.Bullet;

@Repository
public interface BulletRepository extends MongoRepository<Bullet, String> {
}

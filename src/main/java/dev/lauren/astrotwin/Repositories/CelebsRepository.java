package dev.lauren.astrotwin.Repositories;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.lauren.astrotwin.Model.CelebModel;

@Repository
public interface CelebsRepository extends MongoRepository<CelebModel, ObjectId> {
    
    Optional<CelebModel> findByName(String name);


}
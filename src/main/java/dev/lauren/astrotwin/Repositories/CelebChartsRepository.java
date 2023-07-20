
package dev.lauren.astrotwin.Repositories;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import dev.lauren.astrotwin.Model.CelebChartModel;


@Repository
public interface CelebChartsRepository extends MongoRepository<CelebChartModel, ObjectId> {
    
     //Optional<CelebChartModel> findByCeleb(CelebModel celeb);
}
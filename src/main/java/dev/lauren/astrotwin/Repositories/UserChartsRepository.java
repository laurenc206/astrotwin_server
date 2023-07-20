
package dev.lauren.astrotwin.Repositories;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import dev.lauren.astrotwin.Model.UserChartModel;

@Repository
public interface UserChartsRepository extends MongoRepository<UserChartModel, ObjectId> {

}
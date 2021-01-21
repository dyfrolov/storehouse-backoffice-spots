package storehouse.backoffice.spots.domain.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import storehouse.backoffice.spots.domain.entities.EntitySpot;

public interface ISpotRepoMongo extends MongoRepository<EntitySpot, String> {

	List<EntitySpot> findAllBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(Integer row, Integer shelf, Integer place);

	List<EntitySpot> findAllByProductProductId(long productId);

	boolean existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(int row, int shelf, int place);

	EntitySpot findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(int row, int shelf, int place);


}

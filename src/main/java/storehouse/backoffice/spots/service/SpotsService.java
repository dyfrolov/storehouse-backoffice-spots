package storehouse.backoffice.spots.service;

import static storehouse.backoffice.spots.api.ApiConstants.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import storehouse.backoffice.spots.api.ReturnCodes;
import storehouse.backoffice.spots.api.dto.DtoLinks;
import storehouse.backoffice.spots.api.dto.DtoProduct;
import storehouse.backoffice.spots.api.dto.DtoSpot;
import storehouse.backoffice.spots.api.dto.DtoSpotCoord;
import storehouse.backoffice.spots.api.dto.DtoSpotsPaginated;
import storehouse.backoffice.spots.domain.dao.ISpotRepoMongo;
import storehouse.backoffice.spots.domain.entities.EntityProduct;
import storehouse.backoffice.spots.domain.entities.EntitySpot;
import storehouse.backoffice.spots.domain.entities.EntitySpotCoord;
import storehouse.backoffice.spots.interfaces.ISpots;

@Slf4j
@Service
public class SpotsService implements ISpots {
	@Autowired
	ISpotRepoMongo repo;

	@Value("${server.hostname:localhost}")
	String hostname;
	@Value("${server.port:8080}")
	String port;

	private String getServiceHostnameWithEndpoint() {
		if (hostname.equals("localhost"))
			hostname=String.format("http://localhost:%s", port);
		return String.format("%s%s",hostname.trim(),ENDPOINT_SPOTS); 
	}
	
	
	@Override
	public List<DtoSpot> getAllSpots() {
		List<EntitySpot> spotEntities = repo.findAll();
		return spotEntities.stream().map(this::mapEntitySpotToDtoSpot).collect(Collectors.toList());
	}

	@Override
	public DtoSpot getOneSpotInfo(int row, int shelf, int place) {
		List<EntitySpot> spotEntities = 
				repo.findAllBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(row,shelf,place);
		if ( spotEntities.size()== 0) {
			return null;
		}
		return mapEntitySpotToDtoSpot(spotEntities.get(0)) ;
	}

	@Override
	public List<DtoSpot> getProductSpots(long productId) {
		List<EntitySpot> spotEntities =
				repo.findAllByProductProductId(productId);
		return spotEntities.stream().map(this::mapEntitySpotToDtoSpot).collect(Collectors.toList());
	}

	@Override
	public DtoSpotsPaginated getAllSpotsPaginated(int pageNumber, int pageSize) {
		if ( pageNumber < 1 ) pageNumber=1;
		if ( pageSize < 1 ) pageSize=1;
		int pageNumberInRepo = pageNumber - 1;
		Page<EntitySpot> page = repo.findAll(PageRequest.of(pageNumberInRepo, pageSize));
		return mapPageToDtoSpotsPaginated(getServiceHostnameWithEndpoint(),pageNumber, pageSize, page);
	}


	private DtoSpotsPaginated mapPageToDtoSpotsPaginated(String serviceHostnameWithEndpoint, int pageNumber, int pageSize, Page<EntitySpot> page) {
		if (!page.hasContent()) {
			return null;
		}
		String first  = String.format(LINK_FORMAT_STRING, 
				serviceHostnameWithEndpoint,
				1, pageSize );
		String prev  = page.isFirst() ? "" : String.format(LINK_FORMAT_STRING, 
				serviceHostnameWithEndpoint,
				pageNumber - 1, pageSize );
		String next = page.isLast() ? "" : String.format(LINK_FORMAT_STRING, 
				serviceHostnameWithEndpoint,
				pageNumber + 1, pageSize );
		String last = String.format(LINK_FORMAT_STRING, 
				serviceHostnameWithEndpoint,
				page.getTotalPages(), pageSize );
		DtoLinks links = DtoLinks.builder()
				.first(first)
				.prev(prev)
				.next(next)
				.last(last)
				.build();
		List<DtoSpot> spots = page.stream().map(this::mapEntitySpotToDtoSpot).collect(Collectors.toList());
		return DtoSpotsPaginated.builder()
				.links(links)
				.spots(spots)
				.build();
	}

	private DtoSpot mapEntitySpotToDtoSpot(EntitySpot entity) {
		int spotVolumeInUnits = entity.getSpotVolumeInUnits();
		DtoSpotCoord spotCoord = DtoSpotCoord.builder()
				.row(entity.getSpotCoord().getRow())
				.shelf(entity.getSpotCoord().getShelf())
				.place(entity.getSpotCoord().getPlace())
				.build();
		DtoProduct product = DtoProduct.builder()
				.productId(entity.getProduct().getProductId())
				.productName(entity.getProduct().getProductName())
				.productUnit(entity.getProduct().getProductUnit())
				.build();
		return DtoSpot.builder()
				.spotVolumeInUnits(spotVolumeInUnits)
				.spotCoord(spotCoord)
				.product(product)
				.build();
	}


	@Override
	public ReturnCodes addSpot(DtoSpot dtoSpot) {
		if (dtoSpot == null || dtoSpot.getSpotCoord()==null || dtoSpot.getProduct()==null) {
			log.error("Error addSpot - wrong parameters. DtoSpot: {}", dtoSpot);
			return ReturnCodes.WRONG_PARAMETERS;
		}
		boolean isSpotExists = repo.existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( isSpotExists ) {
			log.warn("Warning addSpot - spot already exists. DtoSpot: {}", dtoSpot);
			return ReturnCodes.ALREADY_EXISTS;
		}
		EntitySpot entitySpot = mapDtoSpotToEntitySpot(dtoSpot);
		try {
			repo.save(entitySpot);
			log.debug("Saved addSpot entity:{} ",entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			log.error("Error addSpot entity:{} ERROR:{}",entitySpot, e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			log.error("Error addSpot entity:{} ERROR:{}",entitySpot, e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}

	private EntitySpot mapDtoSpotToEntitySpot(DtoSpot dtoSpot) throws IllegalArgumentException {
		if (dtoSpot == null || dtoSpot.getSpotCoord()==null || dtoSpot.getProduct()==null) {
			throw new IllegalArgumentException();
		}
		int spotVolumeInUnits = dtoSpot.getSpotVolumeInUnits();
		EntitySpotCoord spotCoord = EntitySpotCoord.builder()
				.row(dtoSpot.getSpotCoord().getRow())
				.shelf(dtoSpot.getSpotCoord().getShelf())
				.place(dtoSpot.getSpotCoord().getPlace())
				.build();
		EntityProduct product = EntityProduct.builder()
				.productId(dtoSpot.getProduct().getProductId())
				.productName(dtoSpot.getProduct().getProductName())
				.productUnit(dtoSpot.getProduct().getProductUnit())
				.build();
		EntitySpot entitySpot = EntitySpot.builder()
				.spotVolumeInUnits(spotVolumeInUnits)
				.spotCoord(spotCoord)
				.product(product)
				.build();
		return entitySpot;
	}


	@Override
	public ReturnCodes updateSpot(DtoSpot dtoSpot) {
		if (dtoSpot == null || dtoSpot.getSpotCoord()==null || dtoSpot.getProduct()==null) {
			log.error("Error updateSpot - wrong parameters. DtoSpot: {}", dtoSpot);
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot oldEntitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( oldEntitySpot == null ) {
			log.error("Error updateSpot - entity does not exist. DtoSpot: {}", dtoSpot);
			return ReturnCodes.NOT_EXISTS;
		}
		EntitySpot newEntitySpot = mapDtoSpotToEntitySpot(dtoSpot);
		try {
			repo.delete(oldEntitySpot);
			repo.save(newEntitySpot);
			log.debug("Updated spot. New entity: {}. Old entity: {}",newEntitySpot,oldEntitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			log.error("Error updateSpot. New entity: {}. Old entity: {}. Error: {}",newEntitySpot,oldEntitySpot,e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			log.error("Error updateSpot. New entity: {}. Old entity: {}. Error: {}",newEntitySpot,oldEntitySpot,e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}


	@Override
	public ReturnCodes deleteSpot(DtoSpot dtoSpot) {
		if (dtoSpot == null || dtoSpot.getSpotCoord()==null || dtoSpot.getProduct()==null) {
			log.error("Error deleteSpot - wrong parameters. DtoSpot: {}", dtoSpot);
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot entitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( entitySpot == null ) {
			log.error("Error deleteSpot - entity does not exist. DtoSpot: {}", dtoSpot);
			return ReturnCodes.NOT_EXISTS;
		}
		try {
			repo.delete(entitySpot);
			log.debug("Deleted spot. Entity: {}",entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			log.error("Error deleteSpot. Entity: {}. Error: {}",entitySpot,e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			log.error("Error deleteSpot. Entity: {}. Error: {}",entitySpot,e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}
	@Override
	public ReturnCodes deleteSpot(int row, int shelf, int place) {
		if (row < 0 || shelf < 0 || place < 0) {
			log.error("Error deleteSpot - wrong parameters. Row: {}, shelf: {}, place: {}",row,shelf,place);
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot entitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				row,
				shelf,
				place );
		if ( entitySpot == null ) {
			log.error("Error deleteSpot - spot does not exists. Row: {}, shelf: {}, place: {}",row,shelf,place);
			return ReturnCodes.NOT_EXISTS;
		}
		try {
			repo.delete(entitySpot);
			log.debug("Deleted spot. Entity: {}",entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			log.error("Error deleteSpot. Entity: {}. Error: {}",entitySpot,e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			log.error("Error deleteSpot. Entity: {}. Error: {}",entitySpot,e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}

}

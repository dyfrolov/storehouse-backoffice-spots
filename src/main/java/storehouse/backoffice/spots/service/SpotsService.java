package storehouse.backoffice.spots.service;

import static storehouse.backoffice.spots.api.ApiConstants.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import storehouse.backoffice.spots.api.DtoLinks;
import storehouse.backoffice.spots.api.DtoProduct;
import storehouse.backoffice.spots.api.DtoSpot;
import storehouse.backoffice.spots.api.DtoSpotCoord;
import storehouse.backoffice.spots.api.DtoSpotsPaginated;
import storehouse.backoffice.spots.api.ReturnCodes;
import storehouse.backoffice.spots.domain.dao.SpotRepoMongo;
import storehouse.backoffice.spots.domain.entities.EntityProduct;
import storehouse.backoffice.spots.domain.entities.EntitySpot;
import storehouse.backoffice.spots.domain.entities.EntitySpotCoord;

@Service
public class SpotsService implements ISpots {
	@Autowired
	SpotRepoMongo repo;

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
			return ReturnCodes.WRONG_PARAMETERS;
		}
		boolean isSpotExists = repo.existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( isSpotExists ) {
			return ReturnCodes.ALREADY_EXISTS;
		}
		try {
			EntitySpot entitySpot = mapDtoSpotToEntitySpot(dtoSpot);
			repo.save(entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			System.out.println("###Error addSpot "+e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			System.out.println("###Error addSpot "+e.getMessage());
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
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot oldEntitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( oldEntitySpot == null ) {
			return ReturnCodes.NOT_EXISTS;
		}
		try {
			EntitySpot newEntitySpot = mapDtoSpotToEntitySpot(dtoSpot);
			repo.delete(oldEntitySpot);
			repo.save(newEntitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			System.out.println("###Error updateSpot "+e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			System.out.println("###Error updateSpot "+e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}


	@Override
	public ReturnCodes deleteSpot(DtoSpot dtoSpot) {
		if (dtoSpot == null || dtoSpot.getSpotCoord()==null || dtoSpot.getProduct()==null) {
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot entitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				dtoSpot.getSpotCoord().getRow(),
				dtoSpot.getSpotCoord().getShelf(),
				dtoSpot.getSpotCoord().getPlace() );
		if ( entitySpot == null ) {
			return ReturnCodes.NOT_EXISTS;
		}
		try {
			repo.delete(entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			System.out.println("###Error deleteSpot "+e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			System.out.println("###Error deleteSpot "+e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}
	@Override
	public ReturnCodes deleteSpot(int row, int shelf, int place) {
		if (row < 0 || shelf < 0 || place < 0) {
			return ReturnCodes.WRONG_PARAMETERS;
		}
		EntitySpot entitySpot = repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(
				row,
				shelf,
				place );
		if ( entitySpot == null ) {
			return ReturnCodes.NOT_EXISTS;
		}
		try {
			repo.delete(entitySpot);
			return ReturnCodes.OK;
		}catch(IllegalArgumentException e) {
			System.out.println("###Error deleteSpot "+e.getMessage());
			return ReturnCodes.WRONG_PARAMETERS;
		}catch (Exception e) {
			System.out.println("###Error deleteSpot "+e.getMessage());
			return ReturnCodes.CRUD_OPERATION_UNSUCCESSFUL;
		}
	}

}

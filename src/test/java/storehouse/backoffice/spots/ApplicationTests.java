package storehouse.backoffice.spots;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import storehouse.backoffice.spots.api.dto.DescriptionDto;
import storehouse.backoffice.spots.api.dto.DtoProduct;
import storehouse.backoffice.spots.api.dto.DtoSpot;
import storehouse.backoffice.spots.api.dto.DtoSpotCoord;
import storehouse.backoffice.spots.api.dto.DtoSpotsPaginated;
import storehouse.backoffice.spots.controllers.Controller;
import storehouse.backoffice.spots.domain.dao.ISpotRepoMongo;
import storehouse.backoffice.spots.domain.entities.EntityProduct;
import storehouse.backoffice.spots.domain.entities.EntitySpot;
import storehouse.backoffice.spots.domain.entities.EntitySpotCoord;
import storehouse.backoffice.spots.service.SpotsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static storehouse.backoffice.spots.api.ApiConstants.*;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApplicationTests {
	@Autowired
	Controller controller;
	
	@Autowired
	SpotsService service;
	
	@Autowired
	TestRestTemplate restTemplate;

	@MockBean
	ISpotRepoMongo repo;
	
	@LocalServerPort
	private int port;
	
	static final int ROW=1;
	static final int SHELF=2;
	static final int PLACE=3;
	static final int SPOT_VOLUME_IN_UNITS=100;
	static final long PRODUCT_ID=44444444;
	static final String PRODUCT_NAME="RED RED WINE";
	static final String PRODUCT_UNITS="BOX 12 BOTTLES";
	static final int PAGE_NUMBER=1;
	static final int PAGE_SIZE=2;
	static EntitySpot entitySpot;
	static DtoSpot dtoSpot;
	static List<EntitySpot> entitySpots;
	static Page<EntitySpot> pageEntitySpots;
	
	@BeforeAll
	static void beforeAll() {
		EntitySpotCoord entitySpotCoord = new EntitySpotCoord(ROW, SHELF, PLACE);
		EntityProduct entityProduct = new EntityProduct(PRODUCT_ID, PRODUCT_NAME, PRODUCT_UNITS);
		entitySpot = EntitySpot.builder()
				.spotVolumeInUnits(SPOT_VOLUME_IN_UNITS)
				.spotCoord(entitySpotCoord)
				.product(entityProduct)
				.build();
		entitySpots = new ArrayList<>();
		entitySpots.add(entitySpot);
		DtoSpotCoord dtoSpotCoord = new DtoSpotCoord(ROW, SHELF, PLACE);
		DtoProduct dtoProduct = new DtoProduct(PRODUCT_ID, PRODUCT_NAME, PRODUCT_UNITS);
		dtoSpot = DtoSpot.builder()
				.spotVolumeInUnits(SPOT_VOLUME_IN_UNITS)
				.spotCoord(dtoSpotCoord)
				.product(dtoProduct)
				.build();
		pageEntitySpots = new PageImpl<EntitySpot>(entitySpots, 
				PageRequest.of(PAGE_NUMBER-1, PAGE_SIZE), entitySpots.size()); 
	}
	@BeforeEach
	void beforeEach() {
		
	}

	@Test
	void contextLoads() {
		assertNotNull(controller);
		assertNotNull(service);
	}


	/******************************************************************
	 *	GET tests  
	 ******************************************************************/
	@Test
	void testGetAllSpotsWithoutPagination() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.findAll()).thenReturn(entitySpots);
		ResponseEntity<List<DtoSpot>> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<DtoSpot>>(){} );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		assertEquals(1, responseEntity.getBody().size());
		assertEquals(dtoSpot, responseEntity.getBody().get(0));
	}
	
	
	@Test
	void testGetAllSpotsWithPagination() {
		String uri = String.format("http://localhost:%d%s?pageNumber=%d&pageSize=%d", 
				port, ENDPOINT_SPOTS, PAGE_NUMBER, PAGE_SIZE);
		Mockito.when(repo.findAll(PageRequest.of(PAGE_NUMBER-1, PAGE_SIZE))).thenReturn(pageEntitySpots);
		ResponseEntity<DtoSpotsPaginated> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				DtoSpotsPaginated.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		DtoSpotsPaginated dtoSpotsPaginated = responseEntity.getBody();
		assertEquals(1 , dtoSpotsPaginated.getSpots().size());
		assertEquals(dtoSpot , dtoSpotsPaginated.getSpots().get(0));
		assertEquals(uri.split("/")[3] , dtoSpotsPaginated.getLinks().getFirst().split("/")[3]);
		assertEquals(uri.split("/")[3] , dtoSpotsPaginated.getLinks().getLast().split("/")[3]);
		assertEquals("" , dtoSpotsPaginated.getLinks().getPrev());
		assertEquals("" , dtoSpotsPaginated.getLinks().getNext());
	}
	
	@Test
	void testGetExistingSpotByCoord() {
		String uri = String.format("http://localhost:%d%s?row=%d&shelf=%d&place=%d", port, ENDPOINT_SPOTS, ROW, SHELF, PLACE);
		Mockito.when(repo.findAllBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW, SHELF, PLACE)).thenReturn(entitySpots);
		ResponseEntity<DtoSpot> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				DtoSpot.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		assertEquals(dtoSpot, responseEntity.getBody());
	}
	
	@Test
	void testGetNotExistingSpotByCoord() {
		String uri = String.format("http://localhost:%d%s?row=%d&shelf=%d&place=%d", port, ENDPOINT_SPOTS, ROW, SHELF, PLACE);
		Mockito.when(repo.findAllBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW, SHELF, PLACE)).thenReturn(new ArrayList<EntitySpot>());
		ResponseEntity<DtoSpot> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				DtoSpot.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}

	@Test
	void testGetExistingSpotsByProduct() {
		String uri = String.format("http://localhost:%d%s?productId=%d", port, ENDPOINT_SPOTS, PRODUCT_ID);
		Mockito.when(repo.findAllByProductProductId(PRODUCT_ID)).thenReturn(entitySpots);
		ResponseEntity<List<DtoSpot>> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<DtoSpot>>() {} );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		assertEquals(1, responseEntity.getBody().size());
		assertEquals(dtoSpot, responseEntity.getBody().get(0));
	}
	
	@Test
	void testGetNotExistingSpotsByProduct() {
		String uri = String.format("http://localhost:%d%s?productId=%d", port, ENDPOINT_SPOTS, PRODUCT_ID);
		Mockito.when(repo.findAllByProductProductId(PRODUCT_ID)).thenReturn(new ArrayList<EntitySpot>());
		ResponseEntity<List<DtoSpot>> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<DtoSpot>>() {} );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}
	
	/******************************************************************
	 *	POST tests  
	 ******************************************************************/
	@Test
	void testAddSpotSuccess() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(false);
		Mockito.when(repo.save(entitySpot)).thenReturn(entitySpot);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.POST,
				new HttpEntity<DtoSpot>(dtoSpot),
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertFalse(responseEntity.hasBody());
		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
	}
	
	@Test
	void testAddSpotNotSuccessAlreadyExists() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(true);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.POST,
				new HttpEntity<DtoSpot>(dtoSpot),
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertTrue(responseEntity.hasBody());
		assertTrue(responseEntity.getBody() instanceof DescriptionDto);
		assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
	}

	@Test
	void testAddSpotNotSuccessBadRequest() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.existsBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(true);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.POST,
				null,
				String.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	/******************************************************************
	 *	PUT tests  
	 ******************************************************************/
	@Test
	void testUpdateSpotSuccess() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(entitySpot);
		Mockito.when(repo.save(entitySpot)).thenReturn(entitySpot);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.PUT,
				new HttpEntity<DtoSpot>(dtoSpot),
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertFalse(responseEntity.hasBody());
	}
	
	@Test
	void testUpdateSpotNotSuccessNotExists() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(null);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.PUT,
				new HttpEntity<DtoSpot>(dtoSpot),
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		assertTrue(responseEntity.getBody() instanceof DescriptionDto);
	}

	@Test
	void testUpdateSpotNotSuccessBadRequest() {
		String uri = String.format("http://localhost:%d%s", port, ENDPOINT_SPOTS);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(null);
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.PUT,
				null,
				String.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	/******************************************************************
	 *	DELETE tests  
	 ******************************************************************/
	@Test
	void testRemoveSpotSuccess() {
		String uri = String.format("http://localhost:%d%s?row=%d&shelf=%d&place=%d", 
				port, ENDPOINT_SPOTS, ROW, SHELF, PLACE);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(entitySpot);		
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.DELETE,
				null,
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertFalse(responseEntity.hasBody());
	}
	
	@Test
	void testRemoveSpotNotSuccessNotExists() {
		String uri = String.format("http://localhost:%d%s?row=%d&shelf=%d&place=%d", 
				port, ENDPOINT_SPOTS, ROW, SHELF, PLACE);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(null);		
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.DELETE,
				null,
				DescriptionDto.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
		assertTrue(responseEntity.hasBody());
		assertTrue(responseEntity.getBody() instanceof DescriptionDto);
	}

	@Test
	void testRemoveSpotNotSuccessBadRequest() {
		String uri = String.format("http://localhost:%d%s?row=%d&shelf=%d", 
				port, ENDPOINT_SPOTS, ROW, SHELF);
		Mockito.when(repo.findBySpotCoordRowAndSpotCoordShelfAndSpotCoordPlace(ROW,SHELF,PLACE)).thenReturn(null);		
		ResponseEntity<?> responseEntity = this.restTemplate.exchange(
				uri,
				HttpMethod.POST,
				null,
				String.class );
		assertNotNull(responseEntity);
		assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}
	
}

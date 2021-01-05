package storehouse.backoffice.spots.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import storehouse.backoffice.spots.api.DescriptionDto;
import storehouse.backoffice.spots.api.DtoSpot;
import storehouse.backoffice.spots.api.DtoSpotsPaginated;
import storehouse.backoffice.spots.service.ISpots;

import static storehouse.backoffice.spots.api.ApiConstants.*;
import storehouse.backoffice.spots.api.ReturnCodes;

import java.util.List;
@RestController
public class Controller {
	@Autowired
	ISpots spotService;
	
	
	@PostMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> addSpot(@RequestBody DtoSpot dtoSpot){
		ReturnCodes serviceCode = spotService.addSpot(dtoSpot); 
		if (serviceCode == ReturnCodes.ALREADY_EXISTS) {
			return new ResponseEntity<DescriptionDto>(
					new DescriptionDto(DESCRIPTION_SPOT_ALREADY_EXISTS),
					HttpStatus.CONFLICT);
		}
		if (serviceCode == ReturnCodes.OK) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		}
		return ResponseEntity.badRequest().build();
	}
	
	@PutMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> updateSpot(@RequestBody DtoSpot dtoSpot){
		ReturnCodes serviceCode = spotService.updateSpot(dtoSpot); 
		if (serviceCode == ReturnCodes.NOT_EXISTS) {
			return new ResponseEntity<DescriptionDto>(
					new DescriptionDto(DESCRIPTION_SPOT_NOT_EXISTS),
					HttpStatus.NOT_FOUND);
		}
		if (serviceCode == ReturnCodes.OK) {
			return ResponseEntity.status(HttpStatus.OK).build();
		}
		return ResponseEntity.badRequest().build();
	}

//	@DeleteMapping(ENDPOINT_SPOTS)
//	ResponseEntity<?> deleteSpotByDto(@RequestBody DtoSpot dtoSpot){
//		ReturnCodes serviceCode = spotService.deleteSpot(dtoSpot); 
//		if (serviceCode == ReturnCodes.NOT_EXISTS) {
//			return new ResponseEntity<DescriptionDto>(
//					new DescriptionDto(DESCRIPTION_SPOT_NOT_EXISTS),
//					HttpStatus.NOT_FOUND);
//		}
//		if (serviceCode == ReturnCodes.OK) {
//			return ResponseEntity.status(HttpStatus.OK).build();
//		}
//		return ResponseEntity.badRequest().build();
//	}

	@DeleteMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> deleteSpot(
			@RequestParam int row,
			@RequestParam int shelf,
			@RequestParam int place
			){
		ReturnCodes serviceCode = spotService.deleteSpot(row,shelf,place); 
		if (serviceCode == ReturnCodes.NOT_EXISTS) {
			return new ResponseEntity<DescriptionDto>(
					new DescriptionDto(DESCRIPTION_SPOT_NOT_EXISTS),
					HttpStatus.NOT_FOUND);
		}
		if (serviceCode == ReturnCodes.OK) {
			return ResponseEntity.status(HttpStatus.OK).build();
		}
		return ResponseEntity.badRequest().build();
	}
	
	
	@GetMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> getSpotData(
			@RequestParam(required = false) Integer row,
			@RequestParam(required = false) Integer shelf,
			@RequestParam(required = false) Integer place,
			@RequestParam(required = false) Integer productId,
			@RequestParam(required = false) Integer pageNumber,
			@RequestParam(required = false) Integer pageSize
			) {
		// get info about a spot
		if (row!=null && shelf!=null && place!=null && 
				productId==null && pageNumber==null && pageSize == null) {
			DtoSpot spotData = spotService.getOneSpotInfo(row, shelf, place); 
			return new ResponseEntity<>(
					spotData != null ? spotData : null 
					, spotData != null ? HttpStatus.OK : HttpStatus.NO_CONTENT);
		} 
		// get info about spots of a product
		if ( productId != null && 
				pageNumber == null && pageSize == null &&
				row == null && shelf == null && place == null) {
			List <DtoSpot> productSpotsData = spotService.getProductSpots(productId);
			boolean isSpotsDataExists = productSpotsData != null && productSpotsData.size()!=0;
			return new ResponseEntity<>(
					isSpotsDataExists ? productSpotsData : null 
					, isSpotsDataExists ? HttpStatus.OK : HttpStatus.NO_CONTENT );
		}
		// get info about all spots without pagination
		if ( productId == null && pageNumber == null && pageSize == null &&
				row == null && shelf == null && place == null) {
			List <DtoSpot> allSpotsData = spotService.getAllSpots();
			boolean isSpotsDataExists = allSpotsData != null && allSpotsData.size()!=0;
			return new ResponseEntity<>(
					isSpotsDataExists ? allSpotsData : null 
					, isSpotsDataExists ? HttpStatus.OK : HttpStatus.NO_CONTENT );
		}
		// get info about all spots with pagination
		if ( pageNumber != null && pageSize != null && 
				productId == null && 
				row == null && shelf == null && place == null) {
			DtoSpotsPaginated allSpotsDataPaginated = spotService.getAllSpotsPaginated(pageNumber, pageSize);
			boolean isSpotsDataExists = allSpotsDataPaginated != null && allSpotsDataPaginated.getSpots()!=null 
					&& allSpotsDataPaginated.getSpots().size()!=0;
			return new ResponseEntity<>(
					isSpotsDataExists ? allSpotsDataPaginated : null 
					, isSpotsDataExists ? HttpStatus.OK : HttpStatus.NO_CONTENT );
		}
		return ResponseEntity.badRequest().build();
	}
}

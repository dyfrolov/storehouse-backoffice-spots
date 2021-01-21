package storehouse.backoffice.spots.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import storehouse.backoffice.spots.api.ReturnCodes;
import storehouse.backoffice.spots.api.dto.DescriptionDto;
import storehouse.backoffice.spots.api.dto.DtoSpot;
import storehouse.backoffice.spots.api.dto.DtoSpotsPaginated;
import storehouse.backoffice.spots.interfaces.ILogChannelMessage;
import storehouse.backoffice.spots.interfaces.ISpots;
import static storehouse.backoffice.spots.api.ApiConstants.*;
import static storehouse.backoffice.spots.interfaces.ILogChannelMessage.LogMessageType.*;
import java.util.List;

@Slf4j
@RestController
public class Controller {
	static final String BINDING_NAME="log-out-0";
	@Autowired
	ILogChannelMessage logChannelMessage;

	@Autowired
	ISpots spotService;
	
	@Autowired
	StreamBridge streamBridge;
	
	@Value("${spring.application.name:backoffice-spots}")
	String serviceName;
	
	
	@PostMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> addSpot(@RequestBody DtoSpot dtoSpot){
		log.debug("POST. Endpoint {}. DtoSpot received: {}", ENDPOINT_SPOTS, dtoSpot);
		ReturnCodes serviceCode = spotService.addSpot(dtoSpot); 
		sendToLogChannel(serviceCode, String.format( "POST addSpot result: %s. DtoSpot: %s", serviceCode.toString(), dtoSpot.toString() ) );
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
		log.debug("PUT Endpoint {}. DtoSpot received: {}", ENDPOINT_SPOTS, dtoSpot);
		ReturnCodes serviceCode = spotService.updateSpot(dtoSpot); 
		sendToLogChannel(serviceCode, String.format( "PUT updateSpot result: %s. DtoSpot: %s", serviceCode.toString(), dtoSpot.toString() ) );
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

	@DeleteMapping(ENDPOINT_SPOTS)
	ResponseEntity<?> deleteSpot(
			@RequestParam int row,
			@RequestParam int shelf,
			@RequestParam int place
			){
		log.debug("DELETE. Endpoint {}. Data received: row{}, shelf{}, place{}", ENDPOINT_SPOTS, row,shelf,place);
		ReturnCodes serviceCode = spotService.deleteSpot(row,shelf,place); 
		sendToLogChannel( serviceCode, String.format( "DELETE deleteSpot result: %s. Spot parameters: row: %d, shelf:%d, place: %d", serviceCode.toString(), row, shelf, place ) );
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
		log.debug("GET. Endpoint {}. Data received: row: {}, shelf: {}, place: {}, productId: {}, pageNumber: {}, pageSize: {}", 
				ENDPOINT_SPOTS, row,shelf,place,productId, pageNumber, pageSize);
//		get info about a spot
		if (row!=null && shelf!=null && place!=null && 
				productId==null && pageNumber==null && pageSize == null) {
			DtoSpot spotData = spotService.getOneSpotInfo(row, shelf, place); 
			return new ResponseEntity<>(
					spotData != null ? spotData : null 
					, spotData != null ? HttpStatus.OK : HttpStatus.NO_CONTENT);
		} 
//		get info about spots of a product
		if ( productId != null && 
				pageNumber == null && pageSize == null &&
				row == null && shelf == null && place == null) {
			List <DtoSpot> productSpotsData = spotService.getProductSpots(productId);
			boolean isSpotsDataExists = productSpotsData != null && productSpotsData.size()!=0;
			return new ResponseEntity<>(
					isSpotsDataExists ? productSpotsData : null 
					, isSpotsDataExists ? HttpStatus.OK : HttpStatus.NO_CONTENT );
		}
//		get info about all spots without pagination
		if ( productId == null && pageNumber == null && pageSize == null &&
				row == null && shelf == null && place == null) {
			List <DtoSpot> allSpotsData = spotService.getAllSpots();
			boolean isSpotsDataExists = allSpotsData != null && allSpotsData.size()!=0;
			return new ResponseEntity<>(
					isSpotsDataExists ? allSpotsData : null 
					, isSpotsDataExists ? HttpStatus.OK : HttpStatus.NO_CONTENT );
		}
//		get info about all spots with pagination
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

	
	private void sendToLogChannel(ReturnCodes serviceCode ,String message) {
		ILogChannelMessage.LogMessageType type;
		switch (serviceCode) {
		case OK: type = INFO; 
			break;
		case ALREADY_EXISTS: type = WARNING;
			break;
		case NOT_EXISTS: 
		case CRUD_OPERATION_UNSUCCESSFUL: 
		case WRONG_PARAMETERS: 
		default: type = ERROR;
		}
		String outMessage = logChannelMessage.formatLogString(type, serviceName, message);
		streamBridge.send(BINDING_NAME, MessageBuilder.withPayload(outMessage).build());
	}
}

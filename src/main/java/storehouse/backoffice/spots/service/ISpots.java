package storehouse.backoffice.spots.service;

import java.util.List;

import storehouse.backoffice.spots.api.DtoSpot;
import storehouse.backoffice.spots.api.DtoSpotsPaginated;
import storehouse.backoffice.spots.api.ReturnCodes;

public interface ISpots {
	DtoSpot getOneSpotInfo(int row, int shelf, int place);
	List<DtoSpot> getProductSpots(long productId);
	List<DtoSpot> getAllSpots();
	DtoSpotsPaginated getAllSpotsPaginated(int pageNumber, int pageSize);
	ReturnCodes addSpot(DtoSpot dtoSpot);
	ReturnCodes updateSpot(DtoSpot dtoSpot);
	ReturnCodes deleteSpot(DtoSpot dtoSpot);
	ReturnCodes deleteSpot(int row,int shelf,int place);
}


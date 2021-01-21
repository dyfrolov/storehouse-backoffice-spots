package storehouse.backoffice.spots.interfaces;

import java.util.List;

import storehouse.backoffice.spots.api.ReturnCodes;
import storehouse.backoffice.spots.api.dto.DtoSpot;
import storehouse.backoffice.spots.api.dto.DtoSpotsPaginated;

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


package storehouse.backoffice.spots.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DtoSpot {
	private int spotVolumeInUnits;
	private DtoSpotCoord spotCoord;
	private DtoProduct product;
}

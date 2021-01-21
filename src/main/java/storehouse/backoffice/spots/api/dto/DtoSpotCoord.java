package storehouse.backoffice.spots.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtoSpotCoord {
	private int row;
	private int shelf;
	private int place;
}

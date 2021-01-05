package storehouse.backoffice.spots.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntitySpotCoord {
	private int row;
	private int shelf;
	private int place;
}

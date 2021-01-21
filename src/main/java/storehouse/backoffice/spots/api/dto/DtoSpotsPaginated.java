package storehouse.backoffice.spots.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DtoSpotsPaginated {
	private DtoLinks links;
	private List<DtoSpot> spots;
}

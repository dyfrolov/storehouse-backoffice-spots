package storehouse.backoffice.spots.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtoProduct {
	private long productId;
	private String productName;
	private String productUnit;
}

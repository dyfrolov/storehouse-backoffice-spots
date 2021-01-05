package storehouse.backoffice.spots.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityProduct {
	private long productId;
	private String productName;
	private String productUnit;
}

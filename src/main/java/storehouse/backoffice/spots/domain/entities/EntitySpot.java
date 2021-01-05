package storehouse.backoffice.spots.domain.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection="spots") 
public class EntitySpot {
	@Id
	private String _id;
	private int spotVolumeInUnits;
	private EntitySpotCoord spotCoord;
	private EntityProduct product;
}

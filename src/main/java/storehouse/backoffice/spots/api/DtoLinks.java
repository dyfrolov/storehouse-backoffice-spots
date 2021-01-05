package storehouse.backoffice.spots.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DtoLinks {
	private String first;
	private String prev;
	private String next;
	private String last;
}

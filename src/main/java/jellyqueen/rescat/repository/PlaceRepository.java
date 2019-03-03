package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Place;
import jellyqueen.rescat.domain.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PlaceRepository extends CrudRepository<Place, Long> {
    List<Place> findByRegion(Region region);
}

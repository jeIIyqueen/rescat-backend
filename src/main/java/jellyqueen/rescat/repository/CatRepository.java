package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.Cat;
import jellyqueen.rescat.domain.Region;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CatRepository extends CrudRepository<Cat, Long> {
    List<Cat> findByRegion(Region region);
}

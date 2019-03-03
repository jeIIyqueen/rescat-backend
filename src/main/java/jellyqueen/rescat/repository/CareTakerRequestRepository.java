package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.request.CareTakerRequest;
import jellyqueen.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CareTakerRequestRepository extends CrudRepository<CareTakerRequest, Long> {
    List<CareTakerRequest> findAllByIsConfirmedOrderByCreatedAtDesc(Integer isConfirmed);

    Integer countByIsConfirmed(Integer isConfirmed);

    boolean existsCareTakerRequestByWriterAndIsConfirmedAndType(User user, Integer isConfirmed, Integer type);
}

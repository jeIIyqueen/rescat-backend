package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.CarePost;
import jellyqueen.rescat.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostRepository extends CrudRepository<CarePost, Long> {
    List<CarePost> findByTypeAndIsConfirmedOrderByUpdatedAtDesc(Integer type, Integer isConfirmed);

    Iterable<CarePost> findByIsConfirmedOrderByUpdatedAtDesc(Integer isConfirmed);

    List<CarePost> findTop5ByIsConfirmedOrderByUpdatedAtDesc(Integer isConfirmed);

    List<CarePost> findByWriterAndIsConfirmedOrderByUpdatedAtDesc(User writer, Integer isConfirmed);

    List<CarePost> findAllByIsConfirmedOrderByUpdatedAt(Integer isConfirmed);

    Boolean existsCarePostByWriterAndIsConfirmed(User writer, Integer isConfirmed);

    List<CarePost> findAllByIsConfirmedOrderByCreatedAt(Integer isConfirmed);

    Integer countByIsConfirmed(Integer isConfirmed);
}

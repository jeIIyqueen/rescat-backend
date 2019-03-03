package jellyqueen.rescat.repository;

import jellyqueen.rescat.domain.CarePostComment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarePostCommentRepository extends CrudRepository<CarePostComment, Long> {
    List<CarePostComment> findByCarePostIdxOrderByCreatedAtAsc(Long idx);
}

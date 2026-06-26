package uz.sdg.sos.base;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    boolean trash(Long id);

    boolean trash(T entity);

    T findByIdOrThrow(Long id);

    T findByIdOrThrow(Long id, String message);

    T findOneOrThrow(Specification<T> specification);

    T findOneOrThrow(Specification<T> specification, String message);

}

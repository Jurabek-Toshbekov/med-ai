package uz.sdg.sos.base;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;

@NoRepositoryBean
public class BaseRepositoryImpl<T extends BaseEntity> extends SimpleJpaRepository<T, Long> implements BaseRepository<T> {

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public boolean trash(Long id) {
        try {
            T t = findByIdOrThrow(id);
            t.setDeleted(true);
            save(t);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean trash(T entity) {
        return trash(entity.getId());
    }

    @Override
    public T findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new DataNotFoundException("not found : " + id));
    }

    @Override
    public T findByIdOrThrow(Long id, String message) {
        return findById(id).orElseThrow(() -> new DataNotFoundException(message));
    }

    @Override
    public T findOneOrThrow(Specification<T> specification) {
        return findOne(specification).orElseThrow(() -> new DataNotFoundException("not found : " + specification));
    }

    @Override
    public T findOneOrThrow(Specification<T> specification, String message) {
        return findOne(specification).orElseThrow(() -> new DataNotFoundException(message));
    }
}

package uz.sdg.sos.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * SpecificationBuilder
 *
 * @param <V>
 * @param <T>
 */
public class SpecificationBuilder<V extends AbstractSpecification<T>, T extends BaseEntity> extends SerializableImpl {

    private static final Logger logger = LogManager.getLogger("ServiceLogger");

    private final V specification;


    private SpecificationBuilder(V specification) {
        this.specification = specification;
        //add orgSearchCriteria
    }


    /**
     * Throw throw constructor no argConstructor not found
     *
     * @param v   Specification class
     * @param <V> Specification
     * @param <T> Entity
     * @return Specification builder
     */
    public static <V extends AbstractSpecification<T>, T extends BaseEntity> SpecificationBuilder<V, T> create(Class<V> v) {

        try {
            Constructor<V> constructor = v.getConstructor();
            return new SpecificationBuilder<>(constructor.newInstance());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            logger.error(e);
        }

        throw new RuntimeException("No-arg constructor not found");
    }


    public SpecificationBuilder<V, T> addAndCriteria(String key, CriteriaOperation operation, Object value) {

        this.specification.addAndCriteria(
                key,
                operation,
                value);

        return this;
    }

    public SpecificationBuilder<V, T> addAndCriteria(SearchCriteria criteria) {

        this.specification.addAndCriteria(criteria);

        return this;
    }

    public final SpecificationBuilder<V, T> addAndCriteria(List<SearchCriteria> criteriaList) {

        this.specification.addAndCriteria(criteriaList);

        return this;

    }

    public SpecificationBuilder<V, T> addOrCriteria(String key, CriteriaOperation operation, Object value) {

        this.specification.addOrCriteria(
                key,
                operation,
                value);

        return this;
    }

    public SpecificationBuilder<V, T> addOrCriteria(SearchCriteria criteria) {

        this.specification.addOrCriteria(criteria);

        return this;
    }

    public final SpecificationBuilder<V, T> addOrCriteria(List<SearchCriteria> criteriaList) {

        this.specification.addAndCriteria(criteriaList);

        return this;

    }

    public SpecificationBuilder<V, T> addCustomCriteria(String key, CriteriaOperation operation, Object value) {

        this.specification.addCustomCriteria(
                key,
                operation,
                value);

        return this;
    }

    public SpecificationBuilder<V, T> addCustomCriteria(SearchCriteria criteria) {

        this.specification.addCustomCriteria(criteria);

        return this;
    }

    public final SpecificationBuilder<V, T> addCustomCriteria(List<SearchCriteria> criteriaList) {

        this.specification.addCustomCriteria(criteriaList);

        return this;

    }

    public void addOrder(List<OrderCriteria> criteriaList) {

        this.specification.addOrder(criteriaList);

    }

    public final void addOrder(String key,
                               Sort.Direction direction) {

        this.specification.addOrder(key, direction);

    }

    public AbstractSpecification<T> build() {

        return specification;
    }

    @Override
    public String toString() {
        return "SpecificationBuilder{" +
                "specification=" + specification +
                '}';
    }

    public static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        logger.debug("Not called in the context of an HTTP request");
        return null;
    }
}

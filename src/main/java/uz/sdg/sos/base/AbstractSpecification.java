package uz.sdg.sos.base;

import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Every Specification must extend this Custom abstract specification
 *
 * @param <T>
 */
public abstract class AbstractSpecification<T extends BaseEntity> extends SerializableImpl implements Specification<T> {

    private final List<SearchCriteria> andCriteriaList;

    private final List<SearchCriteria> orCriteriaList;

    private final List<SearchCriteria> customCriteriaList;

    private final List<OrderCriteria> orderCriteriaList;

    /**
     * Override constructor without any changes
     */
    public AbstractSpecification() {
        this.customCriteriaList = new ArrayList<>();
        this.andCriteriaList = new ArrayList<>();
        this.orCriteriaList = new ArrayList<>();
        this.orderCriteriaList = new ArrayList<>();
    }

    @Override
    public final Predicate toPredicate(Root<T> root,
                                       CriteriaQuery<?> query,
                                       CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicatesForFilter = new ArrayList<>();

        query.distinct(isQueryDistinct());

        if (orderCriteriaList.size() > 0)
            query.orderBy(toOrderBy(root, orderCriteriaList));

        if (andCriteriaList.size() > 0)
            predicatesForFilter.addAll(toPredicate(root, query, criteriaBuilder, andCriteriaList));


        if (customCriteriaList.size() > 0)
            predicatesForFilter.addAll(toCustomPredicate(root, query, criteriaBuilder, customCriteriaList));


        if (orCriteriaList.size() > 0)
            predicatesForFilter.add(criteriaBuilder.or(
                    toPredicate(root, query, criteriaBuilder, orCriteriaList).toArray(new Predicate[0])));


        return criteriaBuilder.and(predicatesForFilter.toArray(new Predicate[0]));
    }

    public abstract List<Predicate> toCustomPredicate(Root<T> root,
                                                      CriteriaQuery<?> query,
                                                      CriteriaBuilder criteriaBuilder,
                                                      List<SearchCriteria> criteriaList);

    public abstract boolean isQueryDistinct();

    public final Order[] toOrderBy(Root<T> root,
                                   List<OrderCriteria> orderCriteriaList) {
        return orderCriteriaList.stream().map(criteria -> toOrderBy(root, criteria)).toArray(Order[]::new);
    }

    public final Order toOrderBy(Root<T> root, OrderCriteria criteria) {
        return new OrderImpl(root.get(criteria.getKey()), criteria.getDirection().isAscending());
    }

    public final List<Predicate> toPredicate(From<?, ?> root,
                                             CriteriaQuery<?> query,
                                             CriteriaBuilder criteriaBuilder,
                                             List<SearchCriteria> criteriaList) {

        List<Predicate> predicateList = new ArrayList<>();

        String key;
        CriteriaOperation operation;
        Object value;

        for (SearchCriteria criteria : criteriaList) {

            key = criteria.getKey();
            operation = criteria.getOperation();
            value = criteria.getValue();

            switch (operation) {

                case IN:
                    predicateList.add(root.get(key).in(getCollection(value)));
                    break;

                case EQUAL:
                    predicateList.add(criteriaBuilder.equal(root.get(key), value));
                    break;

                case NOT_EQUAL:
                    predicateList.add(criteriaBuilder.notEqual(root.get(key), value));
                    break;

                case IS_NULL:
                    predicateList.add(root.get(key).isNull());
                    break;

                case NOT_NULL:
                    predicateList.add(root.get(key).isNotNull());
                    break;

                case LIKE:
                    predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(key)), getLikePattern(value)));
                    break;

                case NOT_LIKE:
                    predicateList.add(criteriaBuilder.notLike(criteriaBuilder.lower(root.get(key)), getLikePattern(value)));
                    break;
                case JOIN:
                    predicateList.addAll(toPredicate(root.join(key), query, criteriaBuilder, criteria.getCriteriaList()));
                    break;
            }
        }

        return predicateList;
    }

//    public final List<Predicate> toPredicate(Join<?, ?> root,
//                                             CriteriaQuery<?> query,
//                                             CriteriaBuilder criteriaBuilder,
//                                             List<SearchCriteria> criteriaList) {
//
//        List<Predicate> predicateList = new ArrayList<>();
//
//        String key;
//        CriteriaOperation operation;
//        Object value;
//
//        for (SearchCriteria criteria : criteriaList) {
//
//            key = criteria.getKey();
//            operation = criteria.getOperation();
//            value = criteria.getValue();
//
//            switch (operation) {
//
//                case IN:
//                    predicateList.add(root.get(key).in(getCollection(value)));
//                    break;
//
//                case EQUAL:
//                    predicateList.add(criteriaBuilder.equal(root.get(key), value));
//                    break;
//
//                case NOT_EQUAL:
//                    predicateList.add(criteriaBuilder.notEqual(root.get(key), value));
//                    break;
//
//                case IS_NULL:
//                    predicateList.add(root.get(key).isNull());
//                    break;
//
//                case NOT_NULL:
//                    predicateList.add(root.get(key).isNotNull());
//                    break;
//
//                case LIKE:
//                    predicateList.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(key)), getLikePattern(value)));
//                    break;
//
//                case NOT_LIKE:
//                    predicateList.add(criteriaBuilder.notLike(criteriaBuilder.lower(root.get(key)), getLikePattern(value)));
//                    break;
//
//                case JOIN:
//                    predicateList.addAll(toPredicate(root.join(key), query, criteriaBuilder, criteria.getCriteriaList()));
//                    break;
//            }
//        }
//
//        return predicateList;
//    }
//
//


    protected final void addAndCriteria(String key,
                                        CriteriaOperation operation,
                                        Object value) {
        this.andCriteriaList.add(
                SearchCriteria.of(
                        key,
                        operation,
                        value
                )
        );
    }

    protected final void addAndCriteria(SearchCriteria criteria) {
        this.andCriteriaList.add(criteria);
    }

    protected final void addAndCriteria(List<SearchCriteria> criteriaList) {

        this.andCriteriaList.addAll(criteriaList);

    }

    protected final void addOrCriteria(String key,
                                       CriteriaOperation operation,
                                       Object value) {
        this.orCriteriaList.add(
                SearchCriteria.of(
                        key,
                        operation,
                        value
                )
        );
    }

    protected final void addOrCriteria(SearchCriteria criteria) {
        this.orCriteriaList.add(criteria);
    }

    protected final void addCustomCriteria(List<SearchCriteria> criteriaList) {

        this.customCriteriaList.addAll(criteriaList);

    }

    protected final void addCustomCriteria(String key,
                                           CriteriaOperation operation,
                                           Object value) {
        this.customCriteriaList.add(
                SearchCriteria.of(
                        key,
                        operation,
                        value
                )
        );
    }

    protected final void addCustomCriteria(SearchCriteria criteria) {
        this.customCriteriaList.add(criteria);
    }

    protected final void addOrCriteria(List<SearchCriteria> criteriaList) {

        this.orCriteriaList.addAll(criteriaList);

    }

    protected final void addOrder(List<OrderCriteria> criteriaList) {

        this.orderCriteriaList.addAll(criteriaList);

    }

    protected final void addOrder(String key,
                                  Sort.Direction direction) {

        this.orderCriteriaList.add(OrderCriteria.of(key, direction));

    }

    public List<SearchCriteria> getAndCriteriaList() {
        return andCriteriaList;
    }

    public static Collection<?> getCollection(Object o) {
        return (Collection<?>) o;
    }

    public static String getLikePattern(Object o) {
        return "%" + o.toString().toLowerCase() + "%";
    }
}

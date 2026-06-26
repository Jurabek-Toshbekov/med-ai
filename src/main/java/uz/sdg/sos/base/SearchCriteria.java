package uz.sdg.sos.base;

import java.util.List;

public class SearchCriteria extends BaseDto{

    private String key;

    private CriteriaOperation operation;

    private Object value;

    private List<SearchCriteria> criteriaList;

    public SearchCriteria() {
    }

    public static SearchCriteria of(String key, CriteriaOperation operation, Object value) {
        return new SearchCriteria(key, operation, value);
    }

    public SearchCriteria(String key, CriteriaOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CriteriaOperation getOperation() {
        return operation;
    }

    public void setOperation(CriteriaOperation operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<SearchCriteria> getCriteriaList() {
        return criteriaList;
    }

    public void setCriteriaList(List<SearchCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "key='" + key + '\'' +
                ", operation='" + operation + '\'' +
                ", value=" + value +
                '}';
    }
}

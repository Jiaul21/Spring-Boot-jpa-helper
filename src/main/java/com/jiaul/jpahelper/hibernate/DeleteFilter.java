package com.jiaul.jpahelper.hibernate;

public class DeleteFilter implements HibernateFilterable<Boolean> {

    public static final String FILTER = "deletedFilter";
    public static final String PARAMETER = "deleted";
    public static final String CONDITION = "deleted = :deleted";

    @Override
    public String getFilter() {
        return FILTER;
    }

    @Override
    public String getParameter() {
        return PARAMETER;
    }

    @Override
    public String getCondition() {
        return CONDITION;
    }
}

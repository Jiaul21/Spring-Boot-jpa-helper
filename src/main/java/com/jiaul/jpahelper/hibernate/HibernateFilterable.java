package com.jiaul.jpahelper.hibernate;

public interface HibernateFilterable<T> {

    String getFilter();

    String getParameter();

    String getCondition();
}
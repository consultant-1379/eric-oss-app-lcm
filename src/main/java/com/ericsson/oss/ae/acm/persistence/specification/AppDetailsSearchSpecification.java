/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.ae.acm.persistence.specification;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

import com.ericsson.oss.ae.acm.persistence.entity.App;

@AllArgsConstructor
public class AppDetailsSearchSpecification implements Specification<App> {

    private static final long serialVersionUID = 1;

    private String key;

    private transient Object value;

    public Object getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public Predicate toPredicate(Root<App> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.equal(
            criteriaBuilder.lower(root.get(this.getKey().toLowerCase(Locale.ROOT))),
            this.getValue().toString().toLowerCase(Locale.ROOT)
        );
    }
}

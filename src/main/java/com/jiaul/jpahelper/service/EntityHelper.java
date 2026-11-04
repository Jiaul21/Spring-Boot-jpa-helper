package com.jiaul.jpahelper.service;

import com.jiaul.jpahelper.service.base.BaseHelper;
import com.jiaul.jpahelper.service.selector.SelectHelper;
import com.jiaul.jpahelper.service.updater.UpdateHelper;

/**
 * Unified facade interface combining the complete suite of entity helper operations.
 * <p>
 * Unites all operational capabilities into a single contract:
 * <ul>
 *   <li>{@link BaseHelper}: Basic CRUD operations, existence checks, projection-based lookups, ID filtering, and transactional execution proxies.</li>
 *   <li>{@link UpdateHelper}: Targeted single-field updates, dynamic map-based updates, and high-performance CASE-WHEN bulk batch updates.</li>
 *   <li>{@link SelectHelper}: Dynamic nested fetch selector with automatic relation joins, Cartesian product prevention, and metamodel caching.</li>
 * </ul>
 * <p>
 * Applications typically extend this interface for domain entities:
 * <pre>{@code
 * public interface StudentHelper extends EntityHelper<Student, Long> {
 *     // custom domain helper methods
 * }
 * }</pre>
 *
 * @param <E>  the managed entity type
 * @param <ID> the primary key identifier type of the entity
 */
public interface EntityHelper<E, ID> extends SelectHelper<E, ID> {
}

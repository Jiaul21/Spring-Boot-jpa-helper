package com.jiaul.jpahelper.service;

import com.jiaul.jpahelper.repository.BaseRepository;
import com.jiaul.jpahelper.service.selector.SelectHelperImpl;
import com.jiaul.jpahelper.service.updater.UpdateHelperImpl;

/**
 * Base implementation of {@link EntityHelper} providing the full operational stack for an entity.
 * <p>
 * Inherits the complete helper hierarchy:
 * <ul>
 *   <li>{@link Helper}: JPA metamodel extraction, entity name caching, and primary key resolution.</li>
 *   <li>{@link com.jiaul.jpahelper.service.base.BaseHelperImpl}: CRUD, existence validation, and projection queries.</li>
 *   <li>{@link UpdateHelperImpl}: Selective field updates and bulk CASE-WHEN batch updates.</li>
 *   <li>{@link SelectHelperImpl}: Dynamic multi-level relation fetch queries and Cartesian product prevention.</li>
 * </ul>
 * <p>
 * Applications extend this class when defining Spring service beans:
 * <pre>{@code
 * @Service
 * public class StudentHelperImpl extends EntityHelperImpl<Student, Long> implements StudentHelper {
 *
 *     public StudentHelperImpl(StudentRepository repository) {
 *         super(Student.class, repository);
 *     }
 * }
 * }</pre>
 *
 * @param <E>  the managed entity type
 * @param <ID> the primary key identifier type of the entity
 */
public class EntityHelperImpl<E, ID> extends SelectHelperImpl<E, ID> implements EntityHelper<E, ID> {

    /**
     * Constructs a new {@code EntityHelperImpl} for the specified entity class and repository.
     *
     * @param entityClass    the runtime class of the managed entity; must not be {@code null}
     * @param baseRepository the Spring Data repository backing the entity; must not be {@code null}
     * @throws NullPointerException if {@code entityClass} or {@code baseRepository} is {@code null}
     */
    public EntityHelperImpl(Class<E> entityClass, BaseRepository<E, ID> baseRepository) {
        super(entityClass, baseRepository);
    }
}

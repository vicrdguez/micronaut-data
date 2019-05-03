package io.micronaut.data.processor.model;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.Version;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.PropertyElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of {@link PersistentEntity} that operates on the sources.
 *
 * @author graemerocher
 * @since 1.0
 */
@Internal
public class SourcePersistentEntity implements PersistentEntity {

    private final ClassElement classElement;
    private final Map<String, PropertyElement> beanProperties;
    private final PersistentProperty[] id;
    private final PersistentProperty version;

    public SourcePersistentEntity(ClassElement classElement) {
        this.classElement = classElement;
        final List<PropertyElement> beanProperties = classElement.getBeanProperties();
        this.beanProperties = new LinkedHashMap<>(beanProperties.size());
        List<PersistentProperty> id = new ArrayList<>(2);
        PersistentProperty version = null;
        for (PropertyElement beanProperty : beanProperties) {
            if (beanProperty.isReadOnly() || beanProperty.hasStereotype(Transient.class)) {
                continue;
            }
            if (beanProperty.hasStereotype(Id.class)) {
                id.add(new SourcePersistentProperty(this, beanProperty));
            } else if (beanProperty.hasStereotype(Version.class)) {
                version = new SourcePersistentProperty(this, beanProperty);
            } else {
                this.beanProperties.put(beanProperty.getName(), beanProperty);
            }
        }

        this.version = version;
        this.id = id.toArray(new PersistentProperty[0]);
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return classElement.getAnnotationMetadata();
    }

    @Nonnull
    @Override
    public String getName() {
        return classElement.getName();
    }

    @Nullable
    @Override
    public PersistentProperty[] getCompositeIdentity() {
        return id;
    }

    @Nullable
    @Override
    public PersistentProperty getIdentity() {
        if (ArrayUtils.isNotEmpty(id)) {
            return id[0];
        }
        return null;
    }

    @Nullable
    @Override
    public PersistentProperty getVersion() {
        return version;
    }

    @Nonnull
    @Override
    public List<PersistentProperty> getPersistentProperties() {
        return beanProperties.values().stream().map(propertyElement ->
                new SourcePersistentProperty(this, propertyElement)
        )
        .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Association> getAssociations() {
        return beanProperties.values().stream()
                .filter(bp -> bp.hasStereotype(Relation.class))
                .map(propertyElement -> new SourceAssociation(this, propertyElement))
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<Embedded> getEmbedded() {
        return beanProperties.values().stream()
                .filter(this::isEmbedded)
                .map(propertyElement -> new SourceEmbedded(this, propertyElement))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public PersistentProperty getPropertyByName(String name) {
        if (StringUtils.isNotEmpty(name)) {
            final PropertyElement prop = beanProperties.get(name);
            if (prop != null) {
                return new SourcePersistentProperty(this, prop);
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public List<String> getPersistentPropertyNames() {
        return Collections.unmodifiableList(new ArrayList<>(beanProperties.keySet()));
    }

    @Override
    public boolean isOwningEntity(PersistentEntity owner) {
        return true;
    }

    @Nullable
    @Override
    public PersistentEntity getParentEntity() {
        return null;
    }

    public ClassElement getClassElement() {
        return classElement;
    }

    private boolean isEmbedded(PropertyElement bp) {
        return bp.hasStereotype(Relation.class) && bp.getValue(Relation.class, "kind", Relation.Kind.class).orElse(null) == Relation.Kind.EMBEDDED;
    }
}
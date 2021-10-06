package com.sap.dsc.aas.lib.aml.config;

import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.deserialization.EmbeddedDataSpecificationDeserializer;
import io.adminshell.aas.v3.dataformat.core.deserialization.EnumDeserializer;
import io.adminshell.aas.v3.dataformat.json.ReflectionAnnotationIntrospector;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Reference;
import net.enilink.composition.ClassResolver;
import net.enilink.composition.CompositionModule;
import net.enilink.composition.mappers.RoleMapper;
import net.enilink.composition.mappers.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.util.SimpleBeanPropertyDefinition;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sap.dsc.aas.lib.aml.config.jackson.BindingSpecificationDeserializer;
import com.sap.dsc.aas.lib.aml.config.model.AbstractConfigFromAttribute;
import com.sap.dsc.aas.lib.aml.config.model.AssetInformationConfig;
import com.sap.dsc.aas.lib.aml.config.model.BindSpecification;
import com.sap.dsc.aas.lib.aml.config.model.ConfigAmlToAas;
import com.sap.dsc.aas.lib.aml.config.model.IdentifiableConfigSupport;

/**
 * Class for deserializing/parsing AAS JSON documents.
 */
public class ConfigLoader2 {

    protected Injector injector;
    protected JsonMapper mapper;
    protected SimpleAbstractTypeResolver typeResolver;
    protected static Map<Class<?>, com.fasterxml.jackson.databind.JsonDeserializer> customDeserializers = Map.of(
        EmbeddedDataSpecification.class, new EmbeddedDataSpecificationDeserializer(),
        BindSpecification.class, new BindingSpecificationDeserializer());

    private ClassResolver<String> classResolver;
    private RoleMapper<String> roleMapper;

    public ConfigLoader2() {
        injector = Guice.createInjector(createModule());
        roleMapper = injector.getInstance(new Key<RoleMapper<String>>() {
        });
        classResolver = injector.getInstance(new Key<ClassResolver<String>>() {
        });

        initTypeResolver();
        buildMapper();
    }


    /*public ConfigAmlToAas loadConfig(String filePath) throws IOException {
        String data = Files.readString(Paths.get(filePath));
        return mapper.treeToValue(ModelTypeProcessor.preprocess(data), ConfigAmlToAas.class);
    }*/

    public ConfigAmlToAas loadConfig(String filePath) throws IOException {
        String data = Files.readString(Paths.get(filePath));
        return mapper.readValue(new File(filePath), ConfigAmlToAas.class);
    }

    protected com.google.inject.Module createModule() {
        return new CompositionModule<String>() {
            @Provides
            @Singleton
            protected TypeFactory<String> provideTypeFactory() {
                return new TypeFactory<>() {
                    @Override
                    public String createType(String type) {
                        return type;
                    }

                    @Override
                    public String toString(String type) {
                        return type;
                    }
                };
            }
        };
    }

    protected void buildMapper() {
        mapper = JsonMapper.builder()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            //.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .annotationIntrospector(new ReflectionAnnotationIntrospector() {
                public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac, JavaType baseType) {
                    if (ReflectionHelper.SUBTYPES.containsKey(ac.getRawType())) {
                        TypeResolverBuilder<?> result = _constructStdTypeResolverBuilder();
                        result = result.init(JsonTypeInfo.Id.NAME, null);
                        result.inclusion(JsonTypeInfo.As.PROPERTY);
                        result.typeProperty("modelType");
                        result.typeIdVisibility(false);
                        return result;
                    }
                    return super.findTypeResolver(config, ac, baseType);
                }
            })
            // disabled for now until camel case enums are used
            // .addModule(buildEnumModule())
            .addModule(buildImplementationModule())
            .addModule(buildCustomDeserializerModule())
            .build();
        ReflectionHelper.JSON_MIXINS.entrySet().forEach(x -> mapper.addMixIn(x.getKey(), x.getValue()));
    }

    protected SimpleModule buildCustomDeserializerModule() {
        SimpleModule module = new SimpleModule();
        customDeserializers.forEach(module::addDeserializer);
        return module;
    }

    private void initTypeResolver() {
        typeResolver = new SimpleAbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                Class<?> src = type.getRawClass();
                if (!roleMapper.isRecordedConcept(src.getName())) {
                    return null;
                }
                Class<?> dst = classResolver.resolveComposite(Arrays.asList(src.getName()));
                if (dst == null) {
                    return null;
                }
                return config.getTypeFactory().constructSpecializedType(type, dst);
            }
        };
        ReflectionHelper.DEFAULT_IMPLEMENTATIONS.stream()
            .filter(info -> !customDeserializers.containsKey(info.getInterfaceType()))
            .forEach(info -> {
                roleMapper.addConcept(info.getInterfaceType(), info.getInterfaceType().getName());
                roleMapper.addBehaviour(info.getImplementationType(), info.getInterfaceType().getName());
                // add config behaviours for this type
                roleMapper.addBehaviour(AbstractConfigFromAttribute.class, info.getInterfaceType().getName());
            });

        // register concepts
        roleMapper.addConcept(Identifiable.class, Identifiable.class.getName());

        // register custom config classes
        roleMapper.addBehaviour(AssetInformationConfig.class, AssetInformation.class.getName());
        roleMapper.addBehaviour(IdentifiableConfigSupport.class, Identifiable.class.getName());
        roleMapper.addBehaviour(IdentifiableConfigSupport.class, Reference.class.getName());
    }

    protected SimpleModule buildEnumModule() {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer<>(x)));
        return module;
    }

    public static Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... arg) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> name.equals(m.getName()) && Arrays.equals(m.getParameterTypes(), arg))
            .findAny();
    }

    protected SimpleModule buildImplementationModule() {
        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(typeResolver);
        module.setValueInstantiators(new SimpleValueInstantiators() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc,
                ValueInstantiator defaultInstantiator) {
                if (beanDesc.getType().getRawClass().getName().startsWith("object.proxies.")) {
                    return new ValueInstantiator.Delegating(defaultInstantiator) {
                        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
                            Object result = super.createUsingDefault(ctxt);
                            injector.injectMembers(result);
                            return result;
                        }
                    };
                }
                return defaultInstantiator;
            }
        });
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc,
                List<BeanPropertyDefinition> propDefs) {
                Map<BeanPropertyDefinition, BeanPropertyDefinition> toReplace = null;
                for (BeanPropertyDefinition propDef : propDefs) {
                    if (Collection.class.isAssignableFrom(propDef.getRawPrimaryType())
                        && propDef.getPrimaryType().getContentType().getRawClass() == Object.class
                        && propDef.hasSetter()) {
                        Method realSetter = null;
                        Set<Class<?>> seen = new HashSet<>();
                        Queue<Class<?>> interfaces = new LinkedList<>();
                        interfaces.addAll(Arrays.asList(beanDesc.getBeanClass().getInterfaces()));
                        while (!interfaces.isEmpty()) {
                            Class<?> itf = interfaces.remove();
                            if (seen.add(itf)) {
                                Optional<Method> m = getMethod(itf, propDef.getSetter().getName(),
                                    propDef.getSetter().getRawParameterTypes());
                                if (m.isPresent()) {
                                    realSetter = m.get();
                                    BeanPropertyDefinition newPropDef = SimpleBeanPropertyDefinition.construct(config,
                                        new AnnotatedMethod(beanDesc.getClassInfo(), realSetter, null, null),
                                        propDef.getFullName());
                                    if (toReplace == null) {
                                        toReplace = new HashMap<>();
                                    }
                                    toReplace.put(propDef, newPropDef);
                                    break;
                                } else {
                                    // search in parents
                                    interfaces.addAll(Arrays.asList(itf.getInterfaces()));
                                }
                            }
                        }
                        // System.out.println(beanDesc.getBeanClass() + " - " + propDef.getFullName() + " -> " + realSetter);
                    }
                }
                if (toReplace != null) {
                    final Map<BeanPropertyDefinition, BeanPropertyDefinition> toReplaceFinal = toReplace;
                    return propDefs.stream().map(propDef -> {
                        BeanPropertyDefinition newPropDef = toReplaceFinal.get(propDef);
                        return newPropDef != null ? newPropDef : propDef;
                    }).collect(Collectors.toList());
                }
                return super.updateProperties(config, beanDesc, propDefs);
            }
        });
        return module;
    }
}
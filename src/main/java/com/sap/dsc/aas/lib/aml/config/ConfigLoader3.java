package com.sap.dsc.aas.lib.aml.config;

import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.deserialization.EmbeddedDataSpecificationDeserializer;
import io.adminshell.aas.v3.dataformat.core.deserialization.EnumDeserializer;
import io.adminshell.aas.v3.dataformat.json.ReflectionAnnotationIntrospector;
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.LangString;
import net.enilink.composition.CompositionModule;
import net.enilink.composition.mappers.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sap.dsc.aas.lib.aml.config.jackson.BindingSpecificationDeserializer;
import com.sap.dsc.aas.lib.aml.config.model.BindSpecification;
import com.sap.dsc.aas.lib.aml.config.model.Config;
import com.sap.dsc.aas.lib.aml.config.model.ConfigAmlToAas;
import com.sap.dsc.aas.lib.aml.config.model.LegacyConfig;
import com.sap.dsc.aas.lib.aml.config.model.LegacyConfigSupport;

/**
 * Class for deserializing/parsing AAS JSON documents.
 */
public class ConfigLoader3 {

    protected static Map<Class<?>, com.fasterxml.jackson.databind.JsonDeserializer> customDeserializers = Map.of(
        EmbeddedDataSpecification.class, new EmbeddedDataSpecificationDeserializer(),
        BindSpecification.class, new BindingSpecificationDeserializer());
    protected JsonMapper mapper;
    protected SimpleAbstractTypeResolver typeResolver;

    public ConfigLoader3() {
        initTypeResolver();
        buildMapper();
    }

    /*public ConfigAmlToAas loadConfig(String filePath) throws IOException {
        String data = Files.readString(Paths.get(filePath));
        return mapper.treeToValue(ModelTypeProcessor.preprocess(data), ConfigAmlToAas.class);
    }*/

    public static Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... arg) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> name.equals(m.getName()) && Arrays.equals(m.getParameterTypes(), arg))
            .findAny();
    }

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
                    // for all other types null must be returned
                    return null;
                }
            })
            // disabled for now until camel case enums are used
            // .addModule(buildEnumModule())
            .addModule(buildImplementationModule())
            .addModule(buildCustomDeserializerModule())
            .build();
        ReflectionHelper.JSON_MIXINS.entrySet().forEach(x -> {
            mapper.addMixIn(x.getKey(), x.getValue());
        });
    }

    protected SimpleModule buildCustomDeserializerModule() {
        SimpleModule module = new SimpleModule();
        customDeserializers.forEach(module::addDeserializer);
        return module;
    }

    private void initTypeResolver() {
        typeResolver = new SimpleAbstractTypeResolver();
        ReflectionHelper.DEFAULT_IMPLEMENTATIONS.stream()
            .filter(info -> !customDeserializers.containsKey(info.getInterfaceType()))
            .forEach(x -> typeResolver.addMapping(x.getInterfaceType(), x.getImplementationType()));
    }

    protected SimpleModule buildEnumModule() {
        SimpleModule module = new SimpleModule();
        ReflectionHelper.ENUMS.forEach(x -> module.addDeserializer(x, new EnumDeserializer<>(x)));
        return module;
    }

    protected SimpleModule buildImplementationModule() {
        SimpleModule module = new SimpleModule();
        // module.setAbstractTypes(typeResolver);
        module.setValueInstantiators(new SimpleValueInstantiators() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc,
                ValueInstantiator defaultInstantiator) {
                if (ReflectionHelper.isModelInterface(beanDesc.getType().getRawClass())) {
                    JavaType modelType = typeResolver.findTypeMapping(config, beanDesc.getType());
                    return new ValueInstantiator.Delegating(defaultInstantiator) {
                        @Override
                        public boolean canInstantiate() {
                            return true;
                        }

                        @Override
                        public boolean canCreateUsingDefault() {
                            return true;
                        }

                        public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
                            Object target;
                            try {
                                target = modelType.getRawClass().getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new IOException(e);
                            }
                            if (target instanceof Config) {
                                // config interface is directly implemented
                                return target;
                            } else {
                                // create a proxy instance that implements the bean interface and the config interface
                                List<Class<?>> interfaces = new ArrayList<>();
                                interfaces.addAll(Arrays.asList(target.getClass().getInterfaces()));
                                interfaces.add(LegacyConfig.class);
                                LegacyConfig config = new LegacyConfigSupport(target);
                                return Proxy.newProxyInstance(getClass().getClassLoader(),
                                    interfaces.toArray(new Class<?>[interfaces.size()]),
                                    new InvocationHandler() {
                                        @Override
                                        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                                            try {
                                                // route to concrete object - either config or the underlying bean
                                                if (Config.class.isAssignableFrom(method.getDeclaringClass())) {
                                                    return method.invoke(config, args);
                                                }
                                                return method.invoke(target, args);
                                            } catch (Throwable e) {
                                                // can be used to set a breakpoint if something goes wrong
                                                throw e;
                                            }
                                        }
                                    });
                            }
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
                // include all config properties
                if (!LegacyConfig.class.isAssignableFrom(beanDesc.getBeanClass()) &&
                    ReflectionHelper.isModelInterfaceOrDefaultImplementation(beanDesc.getBeanClass())
                    && !LangString.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    Set<String> existingProps = propDefs.stream().map(propDef -> propDef.getName()).collect(Collectors.toSet());
                    List<BeanPropertyDefinition> compoundDefs = new ArrayList<>(propDefs);
                    compoundDefs.addAll(
                        config.introspect(config.getTypeFactory().constructSimpleType(LegacyConfig.class, null))
                            .findProperties().stream()
                            // filter properties that are already contained in base bean interface
                            .filter(propDef -> !existingProps.contains(propDef.getName())).collect(Collectors.toList()));
                    propDefs = compoundDefs;
                }
                return super.updateProperties(config, beanDesc, propDefs);
            }
        });
        return module;
    }
}
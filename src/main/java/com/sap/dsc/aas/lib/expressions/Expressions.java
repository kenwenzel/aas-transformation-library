package com.sap.dsc.aas.lib.expressions;

import static com.sap.dsc.aas.lib.expressions.Helpers.binaryDouble;
import static com.sap.dsc.aas.lib.expressions.Helpers.binaryObj;
import static com.sap.dsc.aas.lib.expressions.Helpers.reduce;
import static com.sap.dsc.aas.lib.expressions.Helpers.unaryDouble;
import static com.sap.dsc.aas.lib.expressions.Helpers.unaryObj;
import static com.sap.dsc.aas.lib.expressions.Helpers.valueToIterator;
import static com.sap.dsc.aas.lib.expressions.Helpers.valueToSet;
import static com.sap.dsc.aas.lib.expressions.Helpers.valueToStream;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.hash.Hashing;

public class Expressions {

    static final Map<String, Expression> constants = new HashMap<>();
    static final Map<String, Function<Object, Object>> functions = new HashMap<>();

    static final ValueUtils values = ValueUtils.getInstance();
    static final ThreadLocal<Object> currentSubject = new ThreadLocal<>();
    static final ThreadLocal<Map<String, Stack<Object>>> currentVars = ThreadLocal.withInitial(() -> new HashMap<>());

    static {
        functions.put("list", args -> valueToStream(args).collect(Collectors.toList()));

        functions.put("set", args -> valueToStream(args).collect(Collectors.toSet()));

        functions.put("range", binaryObj((a, b) -> IntStream.rangeClosed(((Number) a).intValue(), ((Number) b).intValue()) //
            .mapToObj(i -> i).collect(Collectors.toList())));

        functions.put("entry", binaryObj((list, i) -> {
            if ((int) values.longValue(i) <= 0) {
                throw new IllegalArgumentException("not a positive index: " + i);
            }
            return valueToStream(list).skip((int) values.longValue(i) - 1).findFirst().get();
        }));

        functions.put("block", reduce((a, b) -> b));

        functions.put("negate", unaryObj(values::negate));
        functions.put("max", reduce(Expressions::max));
        functions.put("min", reduce(Expressions::min));
        functions.put("minus", binaryObj(values::subtract));

        functions.put("root", binaryDouble((a, b) -> Math.pow(a, 1 / b)));

        functions.put("intersect", args -> {
            @SuppressWarnings("unchecked")
            Stream<Object> stream = (Stream<Object>) valueToStream(args);
            return stream.reduce(null, (a, b) -> {
                if (a == null) {
                    // the first element
                    return valueToSet(b, true);
                } else {
                    ((Set<?>) a).retainAll(valueToSet(b, false));
                    return a;
                }
            });
        });

        functions.put("sin", unaryDouble(Math::sin));
        functions.put("cos", unaryDouble(Math::cos));
        functions.put("tan", unaryDouble(Math::tan));

        functions.put("arcsin", unaryDouble(Math::asin));
        functions.put("arccos", unaryDouble(Math::acos));
        functions.put("arctan", unaryDouble(Math::atan));

        functions.put("abs", unaryObj(Expressions::abs));
        functions.put("plus", reduce(values::add));
        functions.put("times", reduce(values::multiply));
        functions.put("power", binaryDouble(Math::pow));
        functions.put("divide", binaryObj(values::divide));

        functions.put("eq", binaryObj((a, b) -> {
            if (Objects.equals(a, b)) {
                return true;
            }
            return values.compareWithConversion(a, b) == 0;
        }));
        functions.put("lt", binaryObj((a, b) -> values.compareWithConversion(a, b) < 0));
        functions.put("leq", binaryObj((a, b) -> values.compareWithConversion(a, b) <= 0));
        functions.put("gt", binaryObj((a, b) -> values.compareWithConversion(a, b) > 0));
        functions.put("geq", binaryObj((a, b) -> values.compareWithConversion(a, b) >= 0));
        functions.put("neq", binaryObj((a, b) -> values.compareWithConversion(a, b) != 0));

        functions.put("not", unaryObj(arg -> !values.booleanValue(arg)));
        functions.put("or", reduce((a, b) -> values.booleanValue(a) || values.booleanValue(b)));
        functions.put("and", reduce((a, b) -> values.booleanValue(a) && values.booleanValue(b)));

        functions.put("round", unaryDouble(Expressions::round));
        functions.put("ceiling", unaryDouble(Math::ceil));
        functions.put("floor", unaryDouble(Math::floor));
        functions.put("trunc", unaryDouble(Expressions::truncate));

        constants.put("null", new ConstantExpr(null));
        constants.put("pi", new ConstantExpr(Math.PI));
        constants.put("e", new ConstantExpr(Math.E));
        constants.put("NaN", new ConstantExpr(Double.NaN));
        constants.put("nil", new ConstantExpr(Collections.emptyList()));

        // not actually part of the nums1 CD, but NaN is useless without this check
        functions.put("isNaN", unaryObj(arg -> Double.isNaN(values.doubleValue(arg))));

        functions.put("println", args -> {
            Iterator<?> it = valueToIterator(args);
            while (it.hasNext()) {
                System.out.print(it.next());
                if (it.hasNext()) {
                    System.out.print(" ");
                }
            }
            System.out.println();
            return null;
        });

        // special functions for ID generation
		functions.put("concatenate_and_hash", args -> {
			Stream<Object> stream = (Stream<Object>) valueToStream(args);
			String concatenated = stream.map(Object::toString).collect(Collectors.joining());
			return Hashing.sha256().hashString(concatenated, StandardCharsets.UTF_8).toString();
		});
    }

    public static Function<Object, Object> getFunctionByName(String name) {
        return functions.get(name);
    }

    public static Expression getConstantByName(String name) {
        return constants.get(name);
    }

    static Object divide(Object a, Object b) {
        // TODO improve for integer division and big decimals
        return values.divide(a, values.doubleValue(b));
    }

    static Object max(Object a, Object b) {
        if (values.compareWithConversion(a, b) >= 0) {
            return a;
        } else {
            return b;
        }
    }

    static Object min(Object a, Object b) {
        if (values.compareWithConversion(a, b) >= 0) {
            return b;
        } else {
            return a;
        }
    }

    static Object abs(Object x) {
        if (values.compareWithConversion(x, 0.0) >= 0) {
            return x;
        } else {
            return values.negate(x);
        }
    }

    static double truncate(double x) {
        return (long) x;
    }

    static double round(double x) {
        return Math.round(x);
    }

    public static Object withSubject(Object subject, Supplier<Object> func) {
        Object last = currentSubject.get();
        try {
            currentSubject.set(subject);
            return func.get();
        } finally {
            currentSubject.set(last);
        }
    }

    public static Object getSubject() {
        return currentSubject.get();
    }

    public static Object withVar(String name, Object value, Supplier<Object> func) {
        Map<String, Stack<Object>> vars = currentVars.get();
        Stack<Object> values = vars.computeIfAbsent(name, varName -> new Stack<>());
        try {
            values.push(value);
            return func.get();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            values.pop();
        }
    }

    public static Object withVars(List<VarBinding> bindings, Supplier<Object> func) {
        Map<String, Stack<Object>> vars = currentVars.get();
        List<Stack<?>> stacks = bindings.stream().map(nameValue -> {
            Stack<Object> valueStack = vars.computeIfAbsent(nameValue.variable, varName -> new Stack<>());
            valueStack.push(nameValue.value);
            return valueStack;
        }).collect(Collectors.toList());
        try {
            return func.get();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            stacks.forEach(stack -> stack.pop());
        }
    }

    public static Object setVar(String name, Object value) {
        Map<String, Stack<Object>> vars = currentVars.get();
        Stack<Object> values = vars.computeIfAbsent(name, varName -> new Stack<>());
        if (!values.isEmpty()) {
            // remove current value
            values.pop();
        }
        // set the new value
        values.push(value);
        return value;
    }

    public static Object getVar(String name) {
        Map<String, Stack<Object>> vars = currentVars.get();
        Stack<Object> values = vars.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.peek();
        }
    }

    public static class VarBinding {

        protected String variable;
        protected Object value;

        public VarBinding(String variable, Object value) {
            this.variable = variable;
            this.value = value;
        }
    }
}
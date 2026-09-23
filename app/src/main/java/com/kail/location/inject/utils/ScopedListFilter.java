package com.kail.location.inject.utils;

import java.util.ArrayList;
import java.util.List;

public class ScopedListFilter {
    public static List<String> getValuesForScope(List<String> values, String scope) {
        return getValuesForScope(values, scope, false);
    }

    public static List<String> getValuesForScope(List<String> values, String scope, boolean keepScopePrefix) {
        StringBuilder scopedValueBuilder;
        ArrayList filteredValues = new ArrayList();
        if (values.isEmpty()) {
            return filteredValues;
        }
        for (String value : values) {
            if (value.contains("|")) {
                String[] parts = value.split("\\|");
                if (parts.length >= 2) {
                    String itemScope = parts[0] + "";
                    value = parts[1] + "";
                    if (itemScope.contains(scope)) {
                        if (keepScopePrefix) {
                            scopedValueBuilder = new StringBuilder();
                            scopedValueBuilder.append(scope);
                            scopedValueBuilder.append("|");
                            scopedValueBuilder.append(value);
                            value = scopedValueBuilder.toString();
                        }
                        filteredValues.add(value);
                    }
                }
            } else {
                if (keepScopePrefix) {
                    scopedValueBuilder = new StringBuilder();
                    scopedValueBuilder.append(scope);
                    scopedValueBuilder.append("|");
                    scopedValueBuilder.append(value);
                    value = scopedValueBuilder.toString();
                }
                filteredValues.add(value);
            }
        }
        return filteredValues;
    }

    public static boolean isAllowed(List<String> rules, String value, String scope) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        List<String> scopedRules = getValuesForScope(rules, scope);
        return (scopedRules.contains("*") || scopedRules.contains(value)) ? false : true;
    }
}

package com.dukani.productservice.infrastructure;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;

@Component
public class OracleConstraintResolver {

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "UK_PRODUCT_NAME", "A product with this name already exists.",
            "UK_PRODUCT_CODE", "A product with this code already exists."
    );

    public String resolve(DataIntegrityViolationException ex) {
        Throwable root = ex.getRootCause();

        if (root instanceof SQLException sqlException) {

            // ORA-00001 = unique constraint violation
            if (sqlException.getErrorCode() == 1) {

                String message = sqlException.getMessage();

                if (message != null) {

                    // Extract constraint name between parentheses
                    int start = message.indexOf('(');
                    int end = message.indexOf(')');

                    if (start > 0 && end > start) {
                        String fullConstraint = message.substring(start + 1, end);

                        // Oracle returns SCHEMA.CONSTRAINT_NAME
                        String constraint =
                                fullConstraint.contains(".")
                                        ? fullConstraint.substring(fullConstraint.indexOf('.') + 1)
                                        : fullConstraint;

                        return CONSTRAINT_MESSAGES.getOrDefault(
                                constraint,
                                "Duplicate resource."
                        );
                    }
                }

                return "Duplicate resource.";
            }
        }

        return "Unexpected database error.";
    }
}

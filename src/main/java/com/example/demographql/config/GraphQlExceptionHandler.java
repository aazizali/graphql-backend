package com.example.demographql.config;

import com.example.demographql.exception.NotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        switch (root) {
            case NotFoundException notFoundException -> {
                return error(root.getMessage(), ErrorType.NOT_FOUND, env);
            }
            case ConstraintViolationException violationException -> {
                String message = violationException.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining("; "));
                return error(message.isBlank() ? "Validation failed" : message, ErrorType.BAD_REQUEST, env);
            }
            case BindException bindException -> {
                String message = bindException.getBindingResult().getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .filter(msg -> msg != null && !msg.isBlank())
                        .distinct()
                        .collect(Collectors.joining("; "));
                return error(message.isBlank() ? "Validation failed" : message, ErrorType.BAD_REQUEST, env);
            }
            case DataIntegrityViolationException dataIntegrityViolationException -> {
                return error("Request violates data constraints", ErrorType.BAD_REQUEST, env);
            }
            default -> {
            }
        }

        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Internal error")
                .build();
    }

    private GraphQLError error(String message, ErrorType type, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(message)
                .build();
    }
}

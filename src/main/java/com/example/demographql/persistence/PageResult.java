package com.example.demographql.persistence;

import java.util.List;

public record PageResult<T>(List<T> items, long total, int offset, int limit) {
}

package ai.mender.domain;

import java.util.List;

public record ListResponse <T> (boolean success, String message, List<T> items) {
}

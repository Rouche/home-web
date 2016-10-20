package eu.daiad.web.model.error;

public enum QueryErrorCode implements ErrorCode {
    EMPTY_QUERY,
    TIME_FILTER_NOT_SET,
    TIME_FILTER_INVALID,
    TIME_FILTER_ABSOLUTE_END_NOT_SET,
    TIME_FILTER_SLIDING_DURATION_NOT_SET,
    SPATIAL_FILTER_GEOMETRY_NOT_SET,
    SPATIAL_FILTER_DISTANCE_NOT_SET,
    POPULATION_FILTER_NOT_SET,
    POPULATION_FILTER_INVALID,
    POPULATION_FILTER_IS_EMPTY,
    POPULATION_FILTER_INVALID_CLUSTER,
    RANKING_TYPE_NOT_SET,
    RANKING_INVALID_LIMIT,
    RANKING_INVALID_FIELD,
    RANKING_INVALID_METRIC,
    METRIC_INVALID,
    SOURCE_INVALID;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}

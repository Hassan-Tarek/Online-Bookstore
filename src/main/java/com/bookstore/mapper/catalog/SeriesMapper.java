package com.bookstore.mapper.catalog;

import com.bookstore.dto.catalog.request.SeriesCreateRequest;
import com.bookstore.dto.catalog.request.SeriesUpdateRequest;
import com.bookstore.dto.catalog.response.SeriesResponse;
import com.bookstore.entity.catalog.Series;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeriesMapper {

    SeriesResponse toResponse(Series series);

    Series toEntity(SeriesCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SeriesUpdateRequest request, @MappingTarget Series series);
}

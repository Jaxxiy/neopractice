package com.example.mapper;

import com.example.dto.ProductDTO;
import com.example.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "dateTimeLastChange", ignore = true)
    ProductDTO toDto(Product product);

    @Mapping(target = "dateTimeLastChange", ignore = true)
    Product toEntity(ProductDTO productDto);

    List<ProductDTO> toDtoList(List<Product> products);

    @Mapping(target = "idProduct", ignore = true)
    @Mapping(target = "dateTimeLastChange", ignore = true)
    void updateProductFromDto(ProductDTO productDto, @MappingTarget Product product);
}
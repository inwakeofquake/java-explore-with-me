package ru.practicum.main_service.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.category.CategoryDto;
import ru.practicum.main_service.dto.category.NewCategoryDto;
import ru.practicum.main_service.entity.Category;
import ru.practicum.main_service.exceptions.ConflictException;
import ru.practicum.main_service.exceptions.NotFoundException;
import ru.practicum.main_service.mapper.CategoryMapper;
import ru.practicum.main_service.repository.CategoryRepository;
import ru.practicum.main_service.repository.EventRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Creating a new category with name: {}", newCategoryDto.getName());
        if (Boolean.TRUE.equals(categoryRepository.existsByName(newCategoryDto.getName()))) {
            log.warn("Category creation conflict: name already used - {}", newCategoryDto.getName());
            throw new ConflictException("Can't create category, name already used by another category");
        }
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Fetching categories from: {} with size: {}", from, size);
        Pageable page = PageRequest.of(from / size, size);
        return categoryMapper.toCategoryDtoList(categoryRepository.findAll(page).toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        log.info("Fetching category by ID: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", catId);
                    return new NotFoundException("Category doesn't exist because ID is not found.");
                });
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Deleting category with ID: {}", catId);
        if (Boolean.TRUE.equals(eventRepository.existsByCategoryId(catId))) {
            log.warn("Conflict in deleting category with ID: {}", catId);
            throw new ConflictException("The category is not empty, category ID not found");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with ID: {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Category not found for update with ID: {}", catId);
                    return new NotFoundException("Category doesn't exist, Category ID not found");
                });
        if (!category.getName().equals(categoryDto.getName())
                && Boolean.TRUE.equals(categoryRepository.existsByName(categoryDto.getName()))) {
            log.warn("Category update conflict: name already used - {}", categoryDto.getName());
            throw new ConflictException("Can't update category, name: already used");
        }
        category.setName(categoryDto.getName());
        return categoryMapper.toCategoryDto(category);
    }
}
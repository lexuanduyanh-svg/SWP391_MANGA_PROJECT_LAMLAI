package com.mangaworkflow.api.service;

import com.mangaworkflow.api.model.SkillCategoryCreateRequest;
import com.mangaworkflow.api.model.SkillCategoryDto;
import com.mangaworkflow.api.model.SkillCategoryUpdateRequest;
import com.mangaworkflow.api.persistence.entity.SkillCategoryEntity;
import com.mangaworkflow.api.persistence.repository.SkillCategoryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemorySkillCategoryService {
  private final SkillCategoryRepository repository;
  private final Map<String, SkillRecord> records = new LinkedHashMap<String, SkillRecord>();
  private final AtomicLong sequence = new AtomicLong(1000);

  public InMemorySkillCategoryService() {
    this(null);
  }

  @Autowired
  public InMemorySkillCategoryService(@Nullable SkillCategoryRepository repository) {
    this.repository = repository;
    seed("1", "Inking", "Line art and clean inking");
    seed("2", "Coloring", "Apply color palettes and shading");
    seed("3", "Background Art", "Create environments and scenery");
    seed("4", "Lettering", "Dialog, sound effects, and typography");
    seed("5", "Shading", "Depth, shadows, and tone work");
    seed("6", "Effects", "Motion lines, impacts, and visual emphasis");
    seedDbIfEmpty();
  }

  public synchronized List<SkillCategoryDto> listSkills() {
    return repository != null ? loadFromDb() : loadFromMemory();
  }

  public synchronized SkillCategoryDto create(SkillCategoryCreateRequest request) {
    return repository != null ? createDb(request) : createMemory(request);
  }

  public synchronized SkillCategoryDto update(String id, SkillCategoryUpdateRequest request) {
    return repository != null ? updateDb(id, request) : updateMemory(id, request);
  }

  public synchronized SkillCategoryDto setActive(String id, boolean active) {
    return repository != null ? setActiveDb(id, active) : setActiveMemory(id, active);
  }

  public synchronized void delete(String id) {
    if (repository != null) {
      SkillCategoryEntity e =
          repository
              .findById(Long.valueOf(id))
              .orElseThrow(() -> new IllegalArgumentException("Skill category not found"));
      repository.delete(e);
      return;
    }
    if (records.remove(id) == null) throw new IllegalArgumentException("Skill category not found");
  }

  private void seed(String id, String name, String description) {
    records.put(id, new SkillRecord(id, name, description, true));
  }

  private void seedDbIfEmpty() {
    if (repository == null || repository.count() > 0) return;
    saveSeed(1L, "Inking", "Line art and clean inking");
    saveSeed(2L, "Coloring", "Apply color palettes and shading");
    saveSeed(3L, "Background Art", "Create environments and scenery");
    saveSeed(4L, "Lettering", "Dialog, sound effects, and typography");
    saveSeed(5L, "Shading", "Depth, shadows, and tone work");
    saveSeed(6L, "Effects", "Motion lines, impacts, and visual emphasis");
  }

  private void saveSeed(Long id, String name, String description) {
    if (repository.findById(id).isPresent()) return;
    SkillCategoryEntity e = new SkillCategoryEntity();
    e.setId(id);
    e.setName(name);
    e.setDescription(description);
    e.setActive(true);
    repository.save(e);
  }

  private List<SkillCategoryDto> loadFromMemory() {
    List<SkillCategoryDto> result = new ArrayList<SkillCategoryDto>();
    for (SkillRecord record : records.values()) result.add(record.toDto());
    return Collections.unmodifiableList(result);
  }

  private List<SkillCategoryDto> loadFromDb() {
    List<SkillCategoryDto> out = new ArrayList<SkillCategoryDto>();
    for (SkillCategoryEntity e : repository.findAll()) out.add(toDto(e));
    return Collections.unmodifiableList(out);
  }

  private SkillCategoryDto createMemory(SkillCategoryCreateRequest request) {
    validateName(request == null ? null : request.getName());
    String normalizedName = normalize(request.getName());
    ensureUnique(normalizedName, null);
    String id = String.valueOf(sequence.incrementAndGet());
    SkillRecord record =
        new SkillRecord(id, request.getName().trim(), trimToNull(request.getDescription()), true);
    records.put(id, record);
    return record.toDto();
  }

  private SkillCategoryDto createDb(SkillCategoryCreateRequest request) {
    validateName(request == null ? null : request.getName());
    String normalizedName = normalize(request.getName());
    if (repository.findByNameIgnoreCase(normalizedName).isPresent())
      throw new IllegalArgumentException("Skill category already exists");
    SkillCategoryEntity e = new SkillCategoryEntity();
    e.setName(request.getName().trim());
    e.setDescription(trimToNull(request.getDescription()));
    e.setActive(true);
    return toDto(repository.save(e));
  }

  private SkillCategoryDto updateMemory(String id, SkillCategoryUpdateRequest request) {
    validateName(request == null ? null : request.getName());
    SkillRecord record = getRequired(id);
    String normalizedName = normalize(request.getName());
    ensureUnique(normalizedName, id);
    record.name = request.getName().trim();
    record.description = trimToNull(request.getDescription());
    return record.toDto();
  }

  private SkillCategoryDto updateDb(String id, SkillCategoryUpdateRequest request) {
    validateName(request == null ? null : request.getName());
    SkillCategoryEntity e =
        repository
            .findById(Long.valueOf(id))
            .orElseThrow(() -> new IllegalArgumentException("Skill category not found"));
    String normalizedName = normalize(request.getName());
    if (repository.existsByNameIgnoreCaseAndIdNot(normalizedName, e.getId()))
      throw new IllegalArgumentException("Skill category already exists");
    e.setName(request.getName().trim());
    e.setDescription(trimToNull(request.getDescription()));
    return toDto(repository.save(e));
  }

  private SkillCategoryDto setActiveMemory(String id, boolean active) {
    SkillRecord record = getRequired(id);
    record.active = active;
    return record.toDto();
  }

  private SkillCategoryDto setActiveDb(String id, boolean active) {
    SkillCategoryEntity e =
        repository
            .findById(Long.valueOf(id))
            .orElseThrow(() -> new IllegalArgumentException("Skill category not found"));
    e.setActive(active);
    return toDto(repository.save(e));
  }

  private SkillRecord getRequired(String id) {
    SkillRecord record = records.get(id);
    if (record == null) throw new IllegalArgumentException("Skill category not found");
    return record;
  }

  private void validateName(String name) {
    if (name == null || name.trim().isEmpty())
      throw new IllegalArgumentException("Name is required");
  }

  private void ensureUnique(String normalizedName, String currentId) {
    for (SkillRecord record : records.values()) {
      if (normalize(record.name).equals(normalizedName)
          && (currentId == null || !record.id.equals(currentId)))
        throw new IllegalArgumentException("Skill category already exists");
    }
  }

  private String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private String trimToNull(String value) {
    return value == null || value.trim().isEmpty() ? null : value.trim();
  }

  private SkillCategoryDto toDto(SkillCategoryEntity e) {
    return new SkillCategoryDto(
        String.valueOf(e.getId()), e.getName(), e.getDescription(), e.isActive());
  }

  private static class SkillRecord {
    private final String id;
    private String name;
    private String description;
    private boolean active;

    private SkillRecord(String id, String name, String description, boolean active) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.active = active;
    }

    private SkillCategoryDto toDto() {
      return new SkillCategoryDto(id, name, description, active);
    }
  }
}

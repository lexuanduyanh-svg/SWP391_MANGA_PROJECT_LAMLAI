package com.mangastudio.workflow.services;

import com.mangastudio.workflow.dtos.SkillCategoryCreateRequest;
import com.mangastudio.workflow.dtos.SkillCategoryDto;
import com.mangastudio.workflow.dtos.SkillCategoryUpdateRequest;
import com.mangastudio.workflow.entities.SkillEntity;
import com.mangastudio.workflow.repositories.SkillRepository;
import java.time.LocalDateTime;
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
  private final SkillRepository skillRepository;
  private final Map<String, SkillRecord> records = new LinkedHashMap<String, SkillRecord>();
  private final AtomicLong sequence = new AtomicLong(1000);

  public InMemorySkillCategoryService() {
    this(null);
  }

  @Autowired
  public InMemorySkillCategoryService(@Nullable SkillRepository skillRepository) {
    this.skillRepository = skillRepository;
    seed("1", "Inking", "Line art and clean inking");
    seed("2", "Coloring", "Apply color palettes and shading");
    seed("3", "Background Art", "Create environments and scenery");
    seed("4", "Lettering", "Dialog, sound effects, and typography");
    seed("5", "Shading", "Depth, shadows, and tone work");
    seed("6", "Effects", "Motion lines, impacts, and visual emphasis");
    seedDbIfEmpty();
  }

  public synchronized List<SkillCategoryDto> listSkills() {
    return useSchemaDb() ? loadFromDb() : loadFromMemory();
  }

  public synchronized SkillCategoryDto create(SkillCategoryCreateRequest request) {
    return useSchemaDb() ? createDb(request) : createMemory(request);
  }

  public synchronized SkillCategoryDto update(String id, SkillCategoryUpdateRequest request) {
    return useSchemaDb() ? updateDb(id, request) : updateMemory(id, request);
  }

  public synchronized SkillCategoryDto setActive(String id, boolean active) {
    return useSchemaDb() ? setActiveDb(id, active) : setActiveMemory(id, active);
  }

  public synchronized void delete(String id) {
    if (useSchemaDb()) {
      skillRepository.deleteById(Long.valueOf(id));
      return;
    }
    if (records.remove(id) == null) {
      throw new IllegalArgumentException("Skill category not found");
    }
  }

  private boolean useSchemaDb() {
    return skillRepository != null;
  }

  private void seed(String id, String name, String description) {
    records.put(id, new SkillRecord(id, name, description, true));
  }

  private void seedDbIfEmpty() {
    if (!useSchemaDb() || skillRepository.count() > 0) {
      return;
    }
    saveSeed("Inking");
    saveSeed("Coloring");
    saveSeed("Background Art");
    saveSeed("Lettering");
    saveSeed("Shading");
    saveSeed("Effects");
  }

  private void saveSeed(String name) {
    SkillEntity e = new SkillEntity();
    e.setSkillName(name);
    e.setCreatedAt(LocalDateTime.now());
    skillRepository.save(e);
  }

  private List<SkillCategoryDto> loadFromMemory() {
    List<SkillCategoryDto> result = new ArrayList<SkillCategoryDto>();
    for (SkillRecord record : records.values()) {
      result.add(record.toDto());
    }
    return Collections.unmodifiableList(result);
  }

  private List<SkillCategoryDto> loadFromDb() {
    List<SkillCategoryDto> out = new ArrayList<SkillCategoryDto>();
    for (SkillEntity e : skillRepository.findAll()) {
      out.add(new SkillCategoryDto(String.valueOf(e.getId()), e.getSkillName(), null, true));
    }
    return Collections.unmodifiableList(out);
  }

  private SkillCategoryDto createMemory(SkillCategoryCreateRequest request) {
    validateName(request == null ? null : request.getName());
    String normalizedName = normalize(request.getName());
    ensureUnique(normalizedName, null);
    String id = String.valueOf(sequence.incrementAndGet());
    SkillRecord record = new SkillRecord(id, request.getName().trim(), trimToNull(request.getDescription()), true);
    records.put(id, record);
    return record.toDto();
  }

  private SkillCategoryDto createDb(SkillCategoryCreateRequest request) {
    validateName(request == null ? null : request.getName());
    if (skillRepository.findBySkillNameIgnoreCase(request.getName().trim()).isPresent()) {
      throw new IllegalArgumentException("Skill category already exists");
    }
    SkillEntity e = new SkillEntity();
    e.setSkillName(request.getName().trim());
    e.setCreatedAt(LocalDateTime.now());
    SkillEntity saved = skillRepository.save(e);
    return new SkillCategoryDto(String.valueOf(saved.getId()), saved.getSkillName(), null, true);
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
    Long skillId = Long.valueOf(id);
    SkillEntity e = skillRepository.findById(skillId).orElseThrow(() -> new IllegalArgumentException("Skill category not found"));
    if (skillRepository.existsBySkillNameIgnoreCaseAndIdNot(request.getName().trim(), skillId)) {
      throw new IllegalArgumentException("Skill category already exists");
    }
    e.setSkillName(request.getName().trim());
    SkillEntity saved = skillRepository.save(e);
    return new SkillCategoryDto(String.valueOf(saved.getId()), saved.getSkillName(), null, true);
  }

  private SkillCategoryDto setActiveMemory(String id, boolean active) {
    SkillRecord record = getRequired(id);
    record.active = active;
    return record.toDto();
  }

  private SkillCategoryDto setActiveDb(String id, boolean active) {
    SkillEntity e = skillRepository.findById(Long.valueOf(id)).orElseThrow(() -> new IllegalArgumentException("Skill category not found"));
    return new SkillCategoryDto(String.valueOf(e.getId()), e.getSkillName(), null, active);
  }

  private SkillRecord getRequired(String id) {
    SkillRecord record = records.get(id);
    if (record == null) {
      throw new IllegalArgumentException("Skill category not found");
    }
    return record;
  }

  private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name is required");
    }
  }

  private void ensureUnique(String normalizedName, String currentId) {
    for (SkillRecord record : records.values()) {
      if (normalize(record.name).equals(normalizedName) && (currentId == null || !record.id.equals(currentId))) {
        throw new IllegalArgumentException("Skill category already exists");
      }
    }
  }

  private String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private String trimToNull(String value) {
    return value == null || value.trim().isEmpty() ? null : value.trim();
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

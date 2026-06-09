package com.mangaworkflow.api.service;

import com.mangaworkflow.api.model.SkillCategoryCreateRequest;
import com.mangaworkflow.api.model.SkillCategoryUpdateRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemorySkillCategoryServiceTest {
  private final InMemorySkillCategoryService service = new InMemorySkillCategoryService();

  @Test
  public void seedsDemoSkills() {
    Assertions.assertTrue(service.listSkills().size() >= 6);
  }

  @Test
  public void createUpdateToggleDeleteWorks() {
    SkillCategoryCreateRequest create = new SkillCategoryCreateRequest();
    create.setName("Sketching");
    create.setDescription("Pencil work");

    String id = service.create(create).getId();

    SkillCategoryUpdateRequest update = new SkillCategoryUpdateRequest();
    update.setName("Sketching Pro");
    update.setDescription("Advanced pencil work");

    Assertions.assertEquals("Sketching Pro", service.update(id, update).getName());
    Assertions.assertFalse(service.setActive(id, false).isActive());

    service.delete(id);
  }
}

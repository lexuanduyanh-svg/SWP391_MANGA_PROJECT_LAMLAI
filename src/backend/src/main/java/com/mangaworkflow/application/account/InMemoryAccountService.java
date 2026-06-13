package com.mangaworkflow.application.account;

import com.mangaworkflow.domain.account.AccountCreateRequest;
import com.mangaworkflow.domain.account.AccountDto;
import com.mangaworkflow.domain.account.AccountStatus;
import com.mangaworkflow.domain.account.AccountUpdateRequest;
import com.mangaworkflow.domain.account.UserRole;
import com.mangaworkflow.domain.skill.SkillCategoryDto;
import com.mangaworkflow.persistence.entity.AccountEntity;
import com.mangaworkflow.persistence.entity.AccountSkillEntity;
import com.mangaworkflow.persistence.entity.SkillCategoryEntity;
import com.mangaworkflow.persistence.repository.AccountRepository;
import com.mangaworkflow.persistence.repository.AccountSkillRepository;
import com.mangaworkflow.persistence.repository.SkillCategoryRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class InMemoryAccountService {
  private final AccountRepository accountRepository;
  private final SkillCategoryRepository skillCategoryRepository;
  private final AccountSkillRepository accountSkillRepository;
  private final Map<String, AccountRecord> accountsById =
      new LinkedHashMap<String, AccountRecord>();
  private final Map<String, String> passwordByEmail = new LinkedHashMap<String, String>();
  private final AtomicLong sequence = new AtomicLong(100);

  public InMemoryAccountService() {
    this(null, null, null);
  }

  @Autowired
  public InMemoryAccountService(
      @Nullable AccountRepository accountRepository,
      @Nullable SkillCategoryRepository skillCategoryRepository,
      @Nullable AccountSkillRepository accountSkillRepository) {
    this.accountRepository = accountRepository;
    this.skillCategoryRepository = skillCategoryRepository;
    this.accountSkillRepository = accountSkillRepository;
    seed("1", "System Admin", "admin@manga.local", "Admin@123", UserRole.Admin);
    seed("2", "Demo Mangaka", "mangaka@manga.local", "Mangaka@123", UserRole.Mangaka);
    seed("3", "Demo Assistant", "assistant@manga.local", "Assistant@123", UserRole.Assistant);
    seed("4", "Demo Tantou Editor", "editor@manga.local", "Editor@123", UserRole.TantouEditor);
    seed(
        "5",
        "Demo Editorial Board Member",
        "board@manga.local",
        "Board@123",
        UserRole.EditorialBoardMember);
    seed(
        "6",
        "Demo Board Member Two",
        "board2@manga.local",
        "Board2@123",
        UserRole.EditorialBoardMember);
    seed(
        "7",
        "Demo Board Member Three",
        "board3@manga.local",
        "Board3@123",
        UserRole.EditorialBoardMember);
    seedDbIfEmpty();
  }

  public synchronized List<AccountDto> listAccounts() {
    return accountRepository != null ? loadFromDb() : loadFromMemory();
  }

  public synchronized Optional<AccountDto> authenticate(String email, String password) {
    return accountRepository != null
        ? authenticateDb(email, password)
        : authenticateMemory(email, password);
  }

  public synchronized AccountDto createAccount(AccountCreateRequest request) {
    return accountRepository != null ? createDb(request) : createMemory(request);
  }

  public synchronized AccountDto updateAccount(String id, AccountUpdateRequest request) {
    return accountRepository != null ? updateDb(id, request) : updateMemory(id, request);
  }

  public synchronized AccountDto setStatus(String id, AccountStatus status) {
    return accountRepository != null ? setStatusDb(id, status) : setStatusMemory(id, status);
  }

  public synchronized AccountDto updateSkills(String id, List<String> skillIds) {
    return accountRepository != null
        ? updateSkillsDb(id, skillIds)
        : updateSkillsMemory(id, skillIds);
  }

  public synchronized void deleteAccount(String id) {
    if (accountRepository != null) {
      accountRepository.deleteById(Long.valueOf(id));
      return;
    }
    AccountRecord removed = accountsById.remove(id);
    if (removed != null) passwordByEmail.remove(removed.email);
  }

  private void seed(String id, String fullName, String email, String password, UserRole role) {
    AccountRecord record =
        new AccountRecord(id, fullName, normalizeEmail(email), role, AccountStatus.Active);
    accountsById.put(id, record);
    passwordByEmail.put(record.email, password);
  }

  private void seedDbIfEmpty() {
    if (accountRepository == null) return;
    ensureDefaultSkills();
    saveSeed(1L, "System Admin", "admin@manga.local", "Admin@123", UserRole.Admin);
    saveSeed(2L, "Demo Mangaka", "mangaka@manga.local", "Mangaka@123", UserRole.Mangaka);
    saveSeed(3L, "Demo Assistant", "assistant@manga.local", "Assistant@123", UserRole.Assistant);
    saveSeed(4L, "Demo Tantou Editor", "editor@manga.local", "Editor@123", UserRole.TantouEditor);
    saveSeed(
        5L,
        "Demo Editorial Board Member",
        "board@manga.local",
        "Board@123",
        UserRole.EditorialBoardMember);
    saveSeed(
        6L,
        "Demo Board Member Two",
        "board2@manga.local",
        "Board2@123",
        UserRole.EditorialBoardMember);
    saveSeed(
        7L,
        "Demo Board Member Three",
        "board3@manga.local",
        "Board3@123",
        UserRole.EditorialBoardMember);
    if (accountSkillRepository != null && accountSkillRepository.count() == 0)
      seedDemoAssignments();
  }

  private void saveSeed(Long id, String fullName, String email, String password, UserRole role) {
    if (accountRepository.findById(id).isPresent()) return;
    AccountEntity e = new AccountEntity();
    e.setId(id);
    e.setFullName(fullName);
    e.setEmail(normalizeEmail(email));
    e.setPassword(password);
    e.setRole(role.name());
    e.setStatus(AccountStatus.Active.name());
    e.setCreatedAt(Instant.now());
    e.setUpdatedAt(Instant.now());
    accountRepository.save(e);
  }

  private void ensureDefaultSkills() {
    if (skillCategoryRepository == null) return;
    saveSkillSeed(1L, "Inking", "Line art and clean inking");
    saveSkillSeed(2L, "Coloring", "Apply color palettes and shading");
    saveSkillSeed(3L, "Background Art", "Create environments and scenery");
    saveSkillSeed(4L, "Lettering", "Dialog, sound effects, and typography");
    saveSkillSeed(5L, "Shading", "Depth, shadows, and tone work");
    saveSkillSeed(6L, "Effects", "Motion lines, impacts, and visual emphasis");
  }

  private void saveSkillSeed(Long id, String name, String description) {
    if (skillCategoryRepository.findById(id).isPresent()
        || skillCategoryRepository.findByNameIgnoreCase(name).isPresent()) return;
    SkillCategoryEntity e = new SkillCategoryEntity();
    e.setId(id);
    e.setName(name);
    e.setDescription(description);
    e.setActive(true);
    skillCategoryRepository.save(e);
  }

  private void seedDemoAssignments() {
    updateSkillsDb("3", java.util.Arrays.asList("1", "2"));
    updateSkillsDb("2", java.util.Collections.singletonList("3"));
    updateSkillsDb("4", java.util.Collections.singletonList("3"));
  }

  private List<AccountDto> loadFromMemory() {
    List<AccountDto> result = new ArrayList<AccountDto>();
    for (AccountRecord record : accountsById.values()) result.add(record.toDto());
    return Collections.unmodifiableList(result);
  }

  private List<AccountDto> loadFromDb() {
    List<AccountDto> out = new ArrayList<AccountDto>();
    for (AccountEntity e : accountRepository.findAll()) out.add(toDto(e));
    return Collections.unmodifiableList(out);
  }

  private Optional<AccountDto> authenticateMemory(String email, String password) {
    if (isBlank(email) || isBlank(password)) return Optional.empty();
    String normalizedEmail = normalizeEmail(email);
    AccountRecord record = findByEmail(normalizedEmail);
    String storedPassword = passwordByEmail.get(normalizedEmail);
    if (record == null || record.status != AccountStatus.Active || !password.equals(storedPassword))
      return Optional.empty();
    return Optional.of(record.toDto());
  }

  private Optional<AccountDto> authenticateDb(String email, String password) {
    if (isBlank(email) || isBlank(password)) return Optional.empty();
    AccountEntity e = accountRepository.findByEmailIgnoreCase(normalizeEmail(email)).orElse(null);
    if (e == null
        || !AccountStatus.Active.name().equalsIgnoreCase(e.getStatus())
        || !password.equals(e.getPassword())) return Optional.empty();
    return Optional.of(toDto(e));
  }

  private AccountDto createMemory(AccountCreateRequest request) {
    validateCreateRequest(request);
    String normalizedEmail = normalizeEmail(request.getEmail());
    validateUniqueEmail(normalizedEmail, null);
    String id = String.valueOf(sequence.incrementAndGet());
    AccountRecord record =
        new AccountRecord(
            id,
            request.getFullName().trim(),
            normalizedEmail,
            request.getRole(),
            AccountStatus.Active);
    accountsById.put(id, record);
    passwordByEmail.put(record.email, request.getPassword());
    return record.toDto();
  }

  private AccountDto createDb(AccountCreateRequest request) {
    validateCreateRequest(request);
    String normalizedEmail = normalizeEmail(request.getEmail());
    if (accountRepository.findByEmailIgnoreCase(normalizedEmail).isPresent())
      throw new IllegalArgumentException("Email already exists");
    AccountEntity e = new AccountEntity();
    e.setFullName(request.getFullName().trim());
    e.setEmail(normalizedEmail);
    e.setPassword(request.getPassword());
    e.setRole(request.getRole().name());
    e.setStatus(AccountStatus.Active.name());
    e.setCreatedAt(Instant.now());
    e.setUpdatedAt(Instant.now());
    return toDto(accountRepository.save(e));
  }

  private AccountDto updateMemory(String id, AccountUpdateRequest request) {
    validateUpdateRequest(request);
    AccountRecord record = getRequired(id);
    String normalizedEmail = normalizeEmail(request.getEmail());
    validateUniqueEmail(normalizedEmail, id);
    String existingPassword = passwordByEmail.remove(record.email);
    record.fullName = request.getFullName().trim();
    record.email = normalizedEmail;
    record.role = request.getRole();
    record.status = request.getStatus();
    passwordByEmail.put(record.email, existingPassword == null ? "ChangeMe@123" : existingPassword);
    return record.toDto();
  }

  private AccountDto updateDb(String id, AccountUpdateRequest request) {
    validateUpdateRequest(request);
    Long dbId = Long.valueOf(id);
    AccountEntity e =
        accountRepository
            .findById(dbId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    String normalizedEmail = normalizeEmail(request.getEmail());
    if (accountRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, dbId))
      throw new IllegalArgumentException("Email already exists");
    e.setFullName(request.getFullName().trim());
    e.setEmail(normalizedEmail);
    e.setRole(request.getRole().name());
    e.setStatus(request.getStatus().name());
    e.setUpdatedAt(Instant.now());
    return toDto(accountRepository.save(e));
  }

  private AccountDto setStatusMemory(String id, AccountStatus status) {
    if (status == null) throw new IllegalArgumentException("Status is required");
    AccountRecord record = getRequired(id);
    record.status = status;
    return record.toDto();
  }

  private AccountDto setStatusDb(String id, AccountStatus status) {
    if (status == null) throw new IllegalArgumentException("Status is required");
    AccountEntity e =
        accountRepository
            .findById(Long.valueOf(id))
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    e.setStatus(status.name());
    e.setUpdatedAt(Instant.now());
    return toDto(accountRepository.save(e));
  }

  private AccountRecord getRequired(String id) {
    AccountRecord record = accountsById.get(id);
    if (record == null) throw new IllegalArgumentException("Account not found");
    return record;
  }

  private AccountRecord findByEmail(String normalizedEmail) {
    for (AccountRecord record : accountsById.values())
      if (record.email.equals(normalizedEmail)) return record;
    return null;
  }

  private void validateUniqueEmail(String normalizedEmail, String currentId) {
    AccountRecord record = findByEmail(normalizedEmail);
    if (record != null && (currentId == null || !record.id.equals(currentId)))
      throw new IllegalArgumentException("Email already exists");
  }

  private void validateCreateRequest(AccountCreateRequest request) {
    if (request == null
        || isBlank(request.getFullName())
        || isBlank(request.getEmail())
        || isBlank(request.getPassword())
        || request.getRole() == null)
      throw new IllegalArgumentException("Full name, email, password, and role are required");
  }

  private void validateUpdateRequest(AccountUpdateRequest request) {
    if (request == null
        || isBlank(request.getFullName())
        || isBlank(request.getEmail())
        || request.getRole() == null
        || request.getStatus() == null)
      throw new IllegalArgumentException("Full name, email, role, and status are required");
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private AccountDto toDto(AccountEntity e) {
    return new AccountDto(
        String.valueOf(e.getId()),
        e.getFullName(),
        e.getEmail(),
        UserRole.valueOf(e.getRole()),
        AccountStatus.valueOf(e.getStatus()),
        loadSkillsForAccount(e.getId()));
  }

  private List<SkillCategoryDto> loadSkillsForAccount(Long accountId) {
    List<SkillCategoryDto> out = new ArrayList<SkillCategoryDto>();
    if (accountSkillRepository == null) return out;
    for (AccountSkillEntity link : accountSkillRepository.findByAccountId(accountId)) {
      SkillCategoryEntity skill = link.getSkill();
      if (skill != null)
        out.add(
            new SkillCategoryDto(
                String.valueOf(skill.getId()),
                skill.getName(),
                skill.getDescription(),
                skill.isActive()));
    }
    return out;
  }

  private AccountDto updateSkillsDb(String id, List<String> skillIds) {
    if (skillCategoryRepository == null || accountSkillRepository == null)
      throw new IllegalStateException("Skills not configured");
    Long accountId = Long.valueOf(id);
    AccountEntity account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    accountSkillRepository.deleteAll(accountSkillRepository.findByAccountId(accountId));
    if (skillIds != null)
      for (String skillId : skillIds) {
        if (isBlank(skillId)) continue;
        SkillCategoryEntity skill =
            skillCategoryRepository
                .findById(Long.valueOf(skillId))
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        AccountSkillEntity link = new AccountSkillEntity();
        link.setAccount(account);
        link.setSkill(skill);
        accountSkillRepository.save(link);
      }
    account.setUpdatedAt(Instant.now());
    accountRepository.save(account);
    return toDto(account);
  }

  private AccountDto updateSkillsMemory(String id, List<String> skillIds) {
    AccountRecord record = getRequired(id);
    record.skills.clear();
    if (skillIds != null)
      for (String skillId : skillIds) if (!isBlank(skillId)) record.skills.add(skillId);
    return record.toDto();
  }

  private static class AccountRecord {
    private final String id;
    private String fullName;
    private String email;
    private UserRole role;
    private AccountStatus status;
    private List<String> skills = new ArrayList<String>();

    private AccountRecord(
        String id, String fullName, String email, UserRole role, AccountStatus status) {
      this.id = id;
      this.fullName = fullName;
      this.email = email;
      this.role = role;
      this.status = status;
    }

    private AccountDto toDto() {
      List<SkillCategoryDto> skillDtos = new ArrayList<SkillCategoryDto>();
      for (String skillId : skills)
        skillDtos.add(new SkillCategoryDto(skillId, "Skill " + skillId, null, true));
      return new AccountDto(id, fullName, email, role, status, skillDtos);
    }
  }
}

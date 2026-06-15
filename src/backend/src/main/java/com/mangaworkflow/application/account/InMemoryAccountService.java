package com.mangaworkflow.application.account;

import com.mangaworkflow.domain.account.AccountCreateRequest;
import com.mangaworkflow.domain.account.AccountDto;
import com.mangaworkflow.domain.account.AccountStatus;
import com.mangaworkflow.domain.account.AccountUpdateRequest;
import com.mangaworkflow.domain.account.UserRole;
import com.mangaworkflow.domain.skill.SkillCategoryDto;
import com.mangaworkflow.persistence.entity.RoleEntity;
import com.mangaworkflow.persistence.entity.SkillEntity;
import com.mangaworkflow.persistence.entity.UserEntity;
import com.mangaworkflow.persistence.entity.UserSkillEntity;
import com.mangaworkflow.persistence.entity.UserSkillId;
import com.mangaworkflow.persistence.repository.RoleRepository;
import com.mangaworkflow.persistence.repository.SkillRepository;
import com.mangaworkflow.persistence.repository.UserRepository;
import com.mangaworkflow.persistence.repository.UserSkillRepository;
import java.time.LocalDateTime;
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
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final SkillRepository skillRepository;
  private final UserSkillRepository userSkillRepository;
  private final Map<String, AccountRecord> accountsById = new LinkedHashMap<String, AccountRecord>();
  private final Map<String, String> passwordByEmail = new LinkedHashMap<String, String>();
  private final AtomicLong sequence = new AtomicLong(100);

  public InMemoryAccountService() {
    this(null, null, null, null);
  }

  @Autowired
  public InMemoryAccountService(@Nullable UserRepository userRepository, @Nullable RoleRepository roleRepository, @Nullable SkillRepository skillRepository, @Nullable UserSkillRepository userSkillRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.skillRepository = skillRepository;
    this.userSkillRepository = userSkillRepository;
    seedMemory("1", "System Admin", "admin@manga.local", "Admin@123", UserRole.Admin);
    seedMemory("2", "Demo Mangaka", "mangaka@manga.local", "Mangaka@123", UserRole.Mangaka);
    seedMemory("3", "Demo Assistant", "assistant@manga.local", "Assistant@123", UserRole.Assistant);
    seedMemory("4", "Demo Tantou Editor", "editor@manga.local", "Editor@123", UserRole.TantouEditor);
    seedMemory("5", "Demo Editorial Board Member", "board@manga.local", "Board@123", UserRole.EditorialBoardMember);
    seedMemory("6", "Demo Board Member Two", "board2@manga.local", "Board2@123", UserRole.EditorialBoardMember);
    seedMemory("7", "Demo Board Member Three", "board3@manga.local", "Board3@123", UserRole.EditorialBoardMember);
    seedDbIfEmpty();
  }

  public synchronized List<AccountDto> listAccounts() {
    return useSchemaDb() ? loadFromDb() : loadFromMemory();
  }

  public synchronized Optional<AccountDto> authenticate(String email, String password) {
    return useSchemaDb() ? authenticateDb(email, password) : authenticateMemory(email, password);
  }

  public synchronized AccountDto createAccount(AccountCreateRequest request) {
    return useSchemaDb() ? createDb(request) : createMemory(request);
  }

  public synchronized AccountDto updateAccount(String id, AccountUpdateRequest request) {
    return useSchemaDb() ? updateDb(id, request) : updateMemory(id, request);
  }

  public synchronized AccountDto setStatus(String id, AccountStatus status) {
    return useSchemaDb() ? setStatusDb(id, status) : setStatusMemory(id, status);
  }

  public synchronized AccountDto updateSkills(String id, List<String> skillIds) {
    return useSchemaDb() ? updateSkillsDb(id, skillIds) : updateSkillsMemory(id, skillIds);
  }

  public synchronized void deleteAccount(String id) {
    if (useSchemaDb()) {
      userSkillRepository.deleteByUser_Id(Long.valueOf(id));
      userRepository.deleteById(Long.valueOf(id));
      return;
    }
    AccountRecord removed = accountsById.remove(id);
    if (removed != null) {
      passwordByEmail.remove(removed.email);
    }
  }

  private boolean useSchemaDb() {
    return userRepository != null && roleRepository != null && skillRepository != null && userSkillRepository != null;
  }

  private void seedDbIfEmpty() {
    if (!useSchemaDb()) {
      return;
    }
    seedRolesIfMissing();
    seedSkillsIfMissing();
    if (userRepository.count() == 0) {
      seedUsersIfEmpty();
    }
  }

  private void seedMemory(String id, String fullName, String email, String password, UserRole role) {
    AccountRecord record = new AccountRecord(id, fullName, normalizeEmail(email), role, AccountStatus.Active);
    accountsById.put(id, record);
    passwordByEmail.put(record.email, password);
  }

  private void seedRolesIfMissing() {
    seedRole("Admin");
    seedRole("Mangaka");
    seedRole("Assistant");
    seedRole("Editor");
    seedRole("Board");
  }
  private void seedRole(String roleName) {
    for (RoleEntity r : roleRepository.findAll()) {
      if (roleName.equalsIgnoreCase(r.getRoleName())) {
        return;
      }
    }
    RoleEntity r = new RoleEntity();
    r.setRoleName(roleName);
    roleRepository.save(r);
  }
  private void seedSkillsIfMissing() {
    if (skillRepository.count() > 0) {
      return;
    }
    seedSkill("Inking");
    seedSkill("Coloring");
    seedSkill("Background Art");
    seedSkill("Lettering");
    seedSkill("Shading");
    seedSkill("Effects");
  }

  private void seedUsersIfEmpty() {
    seedUser("System Admin", "admin@manga.local", "Admin@123", "Admin");
    seedUser("Demo Mangaka", "mangaka@manga.local", "Mangaka@123", "Mangaka");
    seedUser("Demo Assistant", "assistant@manga.local", "Assistant@123", "Assistant");
    seedUser("Demo Tantou Editor", "editor@manga.local", "Editor@123", "Editor");
    seedUser("Demo Editorial Board Member", "board@manga.local", "Board@123", "Board");
    seedUser("Demo Board Member Two", "board2@manga.local", "Board2@123", "Board");
    seedUser("Demo Board Member Three", "board3@manga.local", "Board3@123", "Board");
  }

  private void seedUser(String fullName, String email, String password, String roleName) {
    UserEntity e = new UserEntity();
    e.setName(fullName);
    e.setEmail(normalizeEmail(email));
    e.setPasswordHash(password);
    e.setRole(resolveRole(roleName));
    e.setStatus("ACTIVE");
    e.setCreatedAt(LocalDateTime.now());
    e.setUpdatedAt(LocalDateTime.now());
    userRepository.save(e);
  }
  private void seedSkill(String skillName) {
    SkillEntity e = new SkillEntity();
    e.setSkillName(skillName);
    e.setCreatedAt(LocalDateTime.now());
    skillRepository.save(e);
  }

  private List<AccountDto> loadFromMemory() {
    List<AccountDto> out = new ArrayList<AccountDto>();
    for (AccountRecord r : accountsById.values()) {
      out.add(r.toDto());
    }
    return Collections.unmodifiableList(out);
  }

  private List<AccountDto> loadFromDb() {
    List<AccountDto> out = new ArrayList<AccountDto>();
    for (UserEntity e : userRepository.findAll()) {
      out.add(toDto(e));
    }
    return Collections.unmodifiableList(out);
  }

  private Optional<AccountDto> authenticateMemory(String email, String password) {
    if (isBlank(email) || isBlank(password)) {
      return Optional.empty();
    }
    AccountRecord r = findByEmail(normalizeEmail(email));
    if (r == null) {
      return Optional.empty();
    }
    if (r.status != AccountStatus.Active) {
      return Optional.empty();
    }
    String stored = passwordByEmail.get(r.email);
    return stored != null && stored.equals(password) ? Optional.of(r.toDto()) : Optional.empty();
  }

  private Optional<AccountDto> authenticateDb(String email, String password) {
    if (isBlank(email) || isBlank(password)) {
      return Optional.empty();
    }
    UserEntity e = userRepository.findByEmailIgnoreCase(normalizeEmail(email)).orElse(null);
    if (e == null) {
      return Optional.empty();
    }
    if (!"ACTIVE".equalsIgnoreCase(e.getStatus())) {
      return Optional.empty();
    }
    return password.equals(e.getPasswordHash()) ? Optional.of(toDto(e)) : Optional.empty();
  }

  private AccountDto createMemory(AccountCreateRequest request) {
    validateCreateRequest(request);
    String normalizedEmail = normalizeEmail(request.getEmail());
    validateUniqueEmail(normalizedEmail, null);
    String id = String.valueOf(sequence.incrementAndGet());
    AccountRecord record = new AccountRecord(id, request.getFullName().trim(), normalizedEmail, request.getRole(), AccountStatus.Active);
    accountsById.put(id, record);
    passwordByEmail.put(normalizedEmail, request.getPassword());
    return record.toDto();
  }

  private AccountDto createDb(AccountCreateRequest request) {
    validateCreateRequest(request);
    String normalizedEmail = normalizeEmail(request.getEmail());
    if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }
    UserEntity e = new UserEntity();
    e.setName(request.getFullName().trim());
    e.setEmail(normalizedEmail);
    e.setPasswordHash(request.getPassword());
    e.setRole(resolveRole(request.getRole()));
    e.setStatus("ACTIVE");
    e.setCreatedAt(LocalDateTime.now());
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(userRepository.save(e));
  }

  private AccountDto updateMemory(String id, AccountUpdateRequest request) {
    validateUpdateRequest(request);
    AccountRecord r = getRequired(id);
    String normalizedEmail = normalizeEmail(request.getEmail());
    validateUniqueEmail(normalizedEmail, id);
    r.fullName = request.getFullName().trim();
    if (!r.email.equals(normalizedEmail)) {
      passwordByEmail.remove(r.email);
      r.email = normalizedEmail;
    }
    r.role = request.getRole();
    r.status = request.getStatus();
    return r.toDto();
  }

  private AccountDto updateDb(String id, AccountUpdateRequest request) {
    validateUpdateRequest(request);
    UserEntity e = userRepository.findById(Long.valueOf(id)).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    String normalizedEmail = normalizeEmail(request.getEmail());
    Optional<UserEntity> existing = userRepository.findByEmailIgnoreCase(normalizedEmail);
    if (existing.isPresent() && !String.valueOf(existing.get().getId()).equals(id)) {
      throw new IllegalArgumentException("Email already exists");
    }
    e.setName(request.getFullName().trim());
    e.setEmail(normalizedEmail);
    e.setRole(resolveRole(request.getRole()));
    e.setStatus(mapStatus(request.getStatus()));
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(userRepository.save(e));
  }

  private AccountDto setStatusMemory(String id, AccountStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status is required");
    }
    AccountRecord r = getRequired(id);
    r.status = status;
    return r.toDto();
  }

  private AccountDto setStatusDb(String id, AccountStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status is required");
    }
    UserEntity e = userRepository.findById(Long.valueOf(id)).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    e.setStatus(mapStatus(status));
    e.setUpdatedAt(LocalDateTime.now());
    return toDto(userRepository.save(e));
  }

  private AccountDto updateSkillsMemory(String id, List<String> skillIds) {
    AccountRecord r = getRequired(id);
    r.skills.clear();
    if (skillIds != null) {
      for (String sid : skillIds) {
        if (!isBlank(sid)) {
          r.skills.add(sid);
        }
      }
    }
    return r.toDto();
  }

  private AccountDto updateSkillsDb(String id, List<String> skillIds) {
    Long accountId = Long.valueOf(id);
    UserEntity account = userRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    userSkillRepository.deleteByUser_Id(accountId);
    if (skillIds != null) {
      for (String sid : skillIds) {
        if (isBlank(sid)) {
          continue;
        }
        SkillEntity skill = skillRepository.findById(Long.valueOf(sid)).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        UserSkillEntity link = new UserSkillEntity();
        UserSkillId linkId = new UserSkillId();
        linkId.setUserId(accountId);
        linkId.setSkillId(skill.getId());
        link.setId(linkId);
        link.setUser(account);
        link.setSkill(skill);
        link.setAssignedAt(LocalDateTime.now());
        userSkillRepository.save(link);
      }
    }
    account.setUpdatedAt(LocalDateTime.now());
    userRepository.save(account);
    return toDto(account);
  }
  private AccountRecord getRequired(String id) { AccountRecord r = accountsById.get(id); if (r == null) throw new IllegalArgumentException("Account not found"); return r; }
  private AccountRecord findByEmail(String normalizedEmail) { for (AccountRecord r : accountsById.values()) { if (r.email.equals(normalizedEmail)) return r; } return null; }
  private void validateUniqueEmail(String normalizedEmail, String currentId) { AccountRecord r = findByEmail(normalizedEmail); if (r != null && (currentId == null || !r.id.equals(currentId))) throw new IllegalArgumentException("Email already exists"); }
  private void validateCreateRequest(AccountCreateRequest request) { if (request == null || isBlank(request.getFullName()) || isBlank(request.getEmail()) || isBlank(request.getPassword()) || request.getRole() == null) throw new IllegalArgumentException("Full name, email, password, and role are required"); }
  private void validateUpdateRequest(AccountUpdateRequest request) { if (request == null || isBlank(request.getFullName()) || isBlank(request.getEmail()) || request.getRole() == null || request.getStatus() == null) throw new IllegalArgumentException("Full name, email, role, and status are required"); }
  private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
  private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
  private String mapStatus(AccountStatus status) { return status == AccountStatus.Active ? "ACTIVE" : "INACTIVE"; }
  private RoleEntity resolveRole(UserRole role) { return resolveRole(mapRole(role)); }
  private RoleEntity resolveRole(String roleName) {
    for (RoleEntity r : roleRepository.findAll()) {
      if (roleName.equalsIgnoreCase(r.getRoleName())) {
        return r;
      }
    }
    RoleEntity r = new RoleEntity();
    r.setRoleName(roleName);
    return roleRepository.save(r);
  }
  private String mapRole(UserRole role) { switch (role) { case Admin: return "Admin"; case Mangaka: return "Mangaka"; case Assistant: return "Assistant"; case TantouEditor: return "Editor"; case EditorialBoardMember: return "Board"; default: throw new IllegalArgumentException("Unsupported role"); } }
  private UserRole mapRoleBack(RoleEntity role) { return mapRoleBack(role == null ? null : role.getRoleName()); }
  private UserRole mapRoleBack(String roleName) { if (roleName == null) throw new IllegalArgumentException("Role is required"); if ("Editor".equalsIgnoreCase(roleName)) return UserRole.TantouEditor; if ("Board".equalsIgnoreCase(roleName)) return UserRole.EditorialBoardMember; return UserRole.valueOf(roleName); }
  private AccountStatus mapStatusBack(String status) { return "ACTIVE".equalsIgnoreCase(status) ? AccountStatus.Active : AccountStatus.Inactive; }
  private AccountDto toDto(UserEntity e) { return new AccountDto(String.valueOf(e.getId()), e.getName(), e.getEmail(), mapRoleBack(e.getRole()), mapStatusBack(e.getStatus()), loadSkillsForAccount(e.getId())); }
  private List<SkillCategoryDto> loadSkillsForAccount(Long accountId) { List<SkillCategoryDto> out = new ArrayList<SkillCategoryDto>(); for (UserSkillEntity link : userSkillRepository.findByUser_Id(accountId)) { if (link.getSkill() == null) { continue; } out.add(new SkillCategoryDto(String.valueOf(link.getSkill().getId()), link.getSkill().getSkillName(), null, true)); } return out; }

  private static class AccountRecord {
    private final String id;
    private String fullName;
    private String email;
    private UserRole role;
    private AccountStatus status;
    private List<String> skills = new ArrayList<String>();

    private AccountRecord(String id, String fullName, String email, UserRole role, AccountStatus status) {
      this.id = id;
      this.fullName = fullName;
      this.email = email;
      this.role = role;
      this.status = status;
    }

    private AccountDto toDto() {
      List<SkillCategoryDto> skillDtos = new ArrayList<SkillCategoryDto>();
      for (String skillId : skills) {
        skillDtos.add(new SkillCategoryDto(skillId, "Skill " + skillId, null, true));
      }
      return new AccountDto(id, fullName, email, role, status, skillDtos);
    }
  }
}

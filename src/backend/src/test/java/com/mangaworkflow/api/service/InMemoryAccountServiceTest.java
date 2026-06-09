package com.mangaworkflow.api.service;

import com.mangaworkflow.api.model.AccountCreateRequest;
import com.mangaworkflow.api.model.AccountDto;
import com.mangaworkflow.api.model.AccountStatus;
import com.mangaworkflow.api.model.AccountUpdateRequest;
import com.mangaworkflow.api.model.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InMemoryAccountServiceTest {
  private final InMemoryAccountService service = new InMemoryAccountService();

  @Test
  public void listAccounts_seedsDemoUsers() {
    Assertions.assertTrue(service.listAccounts().size() >= 5);
  }

  @Test
  public void createUpdateDeactivateAccount_works() {
    AccountCreateRequest create = new AccountCreateRequest();
    create.setFullName("New Admin");
    create.setEmail("newadmin@manga.local");
    create.setPassword("NewAdmin@123");
    create.setRole(UserRole.Admin);

    String id = service.createAccount(create).getId();

    AccountUpdateRequest update = new AccountUpdateRequest();
    update.setFullName("Updated Admin");
    update.setEmail("newadmin@manga.local");
    update.setRole(UserRole.Assistant);
    update.setStatus(AccountStatus.Inactive);

    Assertions.assertEquals("Updated Admin", service.updateAccount(id, update).getFullName());
    Assertions.assertEquals(
        AccountStatus.Suspended, service.setStatus(id, AccountStatus.Suspended).getStatus());

    service.deleteAccount(id);
  }

  @Test
  public void createAccount_canAuthenticateWithCreatedPassword() {
    AccountCreateRequest create = new AccountCreateRequest();
    create.setFullName("Demo Created Account");
    create.setEmail("created@manga.local");
    create.setPassword("Created@123");
    create.setRole(UserRole.Mangaka);

    AccountDto created = service.createAccount(create);

    Assertions.assertTrue(service.authenticate(created.getEmail(), "Created@123").isPresent());
  }
}

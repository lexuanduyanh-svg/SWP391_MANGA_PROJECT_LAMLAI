package com.mangastudio.workflow.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "role_permissions")
public class RolePermissionEntity {

  @EmbeddedId
  private RolePermissionId id;

  @ManyToOne
  @MapsId("roleId")
  @JoinColumn(name = "role_id")
  private RoleEntity role;

  @ManyToOne
  @MapsId("permissionId")
  @JoinColumn(name = "permission_id")
  private PermissionEntity permission;

  public RolePermissionId getId() {
    return id;
  }

  public void setId(RolePermissionId id) {
    this.id = id;
  }

  public RoleEntity getRole() {
    return role;
  }

  public void setRole(RoleEntity role) {
    this.role = role;
  }

  public PermissionEntity getPermission() {
    return permission;
  }

  public void setPermission(PermissionEntity permission) {
    this.permission = permission;
  }
}

package com.foomei.core.web.controller;

import com.foomei.core.entity.UserGroup;
import com.foomei.core.service.RoleService;
import com.foomei.core.service.UserGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(description = "机构管理")
@Controller
@RequestMapping(value = "/admin/userGroup")
public class UserGroupController {

  private static final String MENU = "userGroup";

  @Value("${system.theme:ace}")
  private String theme;

  @Autowired
  private UserGroupService userGroupService;
  @Autowired
  private RoleService roleService;

  @ApiOperation(value = "机构列表页面", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping
  public String list(Model model) {
    model.addAttribute("menu", MENU);
    model.addAttribute("roles", roleService.getAll());
    return theme + "/userGroup/userGroupList";
  }

  @ApiOperation(value = "机构新增页面", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "create")
  public String create(Long parentId, Model model) {
    UserGroup userGroup = new UserGroup();
    model.addAttribute("userGroup", userGroup);
    model.addAttribute("parent", parentId == null ? null : userGroupService.get(parentId));
    model.addAttribute("roles", roleService.getAll());
    return theme + "/userGroup/userGroupForm";
  }

  @ApiOperation(value = "机构修改页面", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "update/{id}", method = RequestMethod.GET)
  public String update(@PathVariable("id") Long id, Model model) {
    UserGroup userGroup = userGroupService.get(id);
    model.addAttribute("userGroup", userGroup);
    model.addAttribute("parent", userGroup.getParentId() == null ? null : userGroupService.get(userGroup.getParentId()));
    model.addAttribute("roles", roleService.getAll());
    return theme + "/userGroup/userGroupForm";
  }

  @ApiOperation(value = "机构选择页面", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "select", method = RequestMethod.GET)
  public String select(Model model) {
    return theme + "/userGroupSelect";
  }

}

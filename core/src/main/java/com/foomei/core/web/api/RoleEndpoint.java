package com.foomei.core.web.api;

import com.baidu.unbiz.fluentvalidator.*;
import com.baidu.unbiz.fluentvalidator.jsr303.HibernateSupportedValidator;
import com.foomei.common.dto.PageQuery;
import com.foomei.common.dto.ResponseResult;
import com.foomei.common.mapper.BeanMapper;
import com.foomei.common.mapper.JsonMapper;
import com.foomei.common.persistence.JqGridFilter;
import com.foomei.common.persistence.search.SearchRequest;
import com.foomei.core.dto.RoleDto;
import com.foomei.core.entity.Role;
import com.foomei.core.service.RoleService;
import com.foomei.core.vo.RoleVo;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import java.util.List;
import java.util.Map;

@Api(description = "角色接口")
@RestController
@RequestMapping(value = "/api/role")
public class RoleEndpoint {

  @Autowired
  private RoleService roleService;

  static {
    Map<String, String> mapFields = Maps.newHashMap();
    mapFields.put("permissionList{id}", "permissions{}");
    BeanMapper.registerClassMap(Role.class, RoleVo.class, mapFields);
  }

  @ApiOperation(value = "角色分页列表", httpMethod = "GET", produces = "application/json")
  @RequiresRoles("admin")
  @RequestMapping(value = "page")
  public ResponseResult<Page<RoleDto>> page(PageQuery pageQuery, Boolean advance, HttpServletRequest request) {
    Page<Role> page = null;
    if (BooleanUtils.isTrue(advance)) {
      JqGridFilter jqGridFilter = JsonMapper.INSTANCE.fromJson(request.getParameter("filters"), JqGridFilter.class);
      page = roleService.getPage(new SearchRequest(jqGridFilter, pageQuery));
    } else {
      page = roleService.getPage(new SearchRequest(pageQuery, Role.PROP_CODE, Role.PROP_NAME));
    }
    return ResponseResult.createSuccess(page, Role.class, RoleDto.class);
  }

  @ApiOperation(value = "角色简单分页列表", httpMethod = "GET", produces = "application/json")
  @RequiresRoles("admin")
  @RequestMapping(value = "page2")
  public ResponseResult<List<RoleDto>> page2(PageQuery pageQuery) {
    Page<Role> page = roleService.getPage(new SearchRequest(pageQuery, Role.PROP_CODE, Role.PROP_NAME));
    return ResponseResult.createSuccess(page.getContent(), page.getTotalElements(), Role.class, RoleDto.class);
  }

  @ApiOperation(value = "数据类型保存", httpMethod = "POST")
  @RequiresRoles("admin")
  @RequestMapping(value = "save", method = RequestMethod.POST)
  public ResponseResult save(RoleVo roleVo) {
    ComplexResult result = validate(roleVo);
    if (!result.isSuccess()) {
      return ResponseResult.createParamError(result);
    }

    Role role = null;
    if(roleVo.getId() == null) {
      role = BeanMapper.map(roleVo, Role.class);;
    } else {
      role = roleService.get(roleVo.getId());
      BeanMapper.map(roleVo, role, RoleVo.class, Role.class);
    }

    roleService.save(role);
    return ResponseResult.SUCCEED;
  }

  @ApiOperation(value = "角色删除", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "delete/{id}")
  public ResponseResult delete(@PathVariable("id") Long id) {
    roleService.delete(id);
    return ResponseResult.SUCCEED;
  }

  /**
   * 判断代码的唯一性
   */
  @ApiOperation(value = "检查角色代码是否存在", httpMethod = "GET")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "code", value = "代码", required = true, dataType = "string", paramType = "query")
  })
  @RequestMapping("checkCode")
  public boolean checkCode(Long id, String code) {
    return !roleService.existCode(id, code);
  }

  private ComplexResult validate(RoleVo role) {
    ComplexResult result = FluentValidator.checkAll()
      .on(role, new HibernateSupportedValidator<RoleVo>().setHiberanteValidator(Validation.buildDefaultValidatorFactory().getValidator()))
      .on(role, new ValidatorHandler<RoleVo>() {
        public boolean validate(ValidatorContext context, RoleVo t) {
          if (roleService.existCode(t.getId(), t.getCode())) {
            context.addErrorMsg("代码已经被使用");
            return false;
          }
          return true;
        }
      })
      .doValidate()
      .result(ResultCollectors.toComplex());
    return result;
  }

}

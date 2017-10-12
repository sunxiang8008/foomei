package com.foomei.core.web.api;

import com.foomei.common.dto.PageQuery;
import com.foomei.common.dto.ResponseResult;
import com.foomei.common.mapper.BeanMapper;
import com.foomei.common.mapper.JsonMapper;
import com.foomei.common.persistence.JqGridFilter;
import com.foomei.common.persistence.search.SearchRequest;
import com.foomei.core.dto.ConfigDto;
import com.foomei.core.entity.Config;
import com.foomei.core.service.ConfigService;
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
import java.util.List;

@Api(description = "系统配置接口")
@RestController
@RequestMapping(value = "/api/config")
public class ConfigEndpoint {

  @Autowired
  private ConfigService configService;

  static {
    BeanMapper.registerClassMap(Config.class, ConfigDto.class, false, false);
  }

  @ApiOperation(value = "系统配置分页列表", httpMethod = "GET", produces = "application/json")
  @RequiresRoles("admin")
  @RequestMapping(value = "page", method = RequestMethod.GET)
  public ResponseResult<Page<Config>> page(PageQuery pageQuery, Boolean advance, HttpServletRequest request) {
    Page<Config> page = null;
    if (BooleanUtils.isTrue(advance)) {
      JqGridFilter jqGridFilter = JsonMapper.INSTANCE.fromJson(request.getParameter("filters"), JqGridFilter.class);
      page = configService.getPage(new SearchRequest(jqGridFilter, pageQuery));
    } else {
      page = configService.getPage(new SearchRequest(pageQuery, Config.PROP_CODE, Config.PROP_NAME));
    }

    return ResponseResult.createSuccess(page);
  }

  @ApiOperation(value = "单配置获取", httpMethod = "GET")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "code", value = "键", required = true, dataType = "string", paramType = "path")
  })
  @RequestMapping("get/{code}")
  public ResponseResult<ConfigDto> getByCode(@PathVariable("code") String code) {
    Config config = configService.getByCode(code);
    return ResponseResult.createSuccess(config, ConfigDto.class);
  }

  @ApiOperation(value = "多配置获取", httpMethod = "GET")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "code", value = "键前缀", required = true, dataType = "string", paramType = "path")
  })
  @RequestMapping("find/{code}")
  public ResponseResult<List<ConfigDto>> findByCode(@PathVariable("code") String code) {
    SearchRequest searchRequest = new SearchRequest().addStartWith(Config.PROP_CODE, code + ".");
    List<Config> configs = configService.getList(searchRequest);
    return ResponseResult.createSuccess(configs, Config.class, ConfigDto.class);
  }

  @ApiOperation(value = "配置保存", httpMethod = "POST")
  @RequiresRoles("admin")
  @RequestMapping("save")
  private ResponseResult save(ConfigDto configDto) {
    Config config = resolve(configDto);
    configService.save(config);
    return ResponseResult.SUCCEED;
  }

  @ApiOperation(value = "配置删除", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "delete/{id}")
  public ResponseResult delete(@PathVariable("id") Long id) {
    configService.delete(id);
    return ResponseResult.SUCCEED;
  }

  /**
   * 判断编码的唯一性
   */
  @ApiOperation(value = "检查配置键是否存在", httpMethod = "GET")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "code", value = "键", required = true, dataType = "string", paramType = "query")
  })
  @RequestMapping("checkCode")
  public boolean checkCode(Long id, String code) {
    return !configService.existCode(id, code);
  }

  private Config resolve(ConfigDto configDto) {
    Config config = configService.get(configDto.getId());
    BeanMapper.map(configDto, config, ConfigDto.class, Config.class);
    return config;
  }

}

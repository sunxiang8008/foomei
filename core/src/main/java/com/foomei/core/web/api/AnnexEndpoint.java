package com.foomei.core.web.api;

import com.foomei.common.dto.PageQuery;
import com.foomei.common.dto.ResponseResult;
import com.foomei.common.mapper.JsonMapper;
import com.foomei.common.persistence.JqGridFilter;
import com.foomei.common.persistence.search.SearchRequest;
import com.foomei.core.dto.AnnexDto;
import com.foomei.core.entity.Annex;
import com.foomei.core.service.AnnexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Api(description = "附件接口")
@RestController
@RequestMapping(value = "/api/annex")
public class AnnexEndpoint {

  @Autowired
  private AnnexService annexService;

  @ApiOperation(value = "附件分页列表", httpMethod = "GET", produces = "application/json")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "startTime", value = "开始时间(yyyy-MM-dd HH:mm)", dataType = "date", paramType = "query"),
    @ApiImplicitParam(name = "endTime", value = "结束时间(yyyy-MM-dd HH:mm)", dataType = "date", paramType = "query")
  })
  @RequiresRoles("admin")
  @RequestMapping(value = "page")
  public ResponseResult<Page<AnnexDto>> page(PageQuery pageQuery, Boolean advance,
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date startTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Date endTime,
                                          HttpServletRequest request) {
    Page<Annex> page = null;
    if (BooleanUtils.isTrue(advance)) {
      JqGridFilter jqGridFilter = JsonMapper.INSTANCE.fromJson(request.getParameter("filters"), JqGridFilter.class);
      page = annexService.getPage(new SearchRequest(jqGridFilter, pageQuery));
    } else {
      page = annexService.getPage(pageQuery.getSearchKey(), startTime, endTime, pageQuery.buildPageRequest(new Sort(Sort.Direction.DESC, Annex.PROP_CREATE_TIME)));
    }
    return ResponseResult.createSuccess(page, Annex.class, AnnexDto.class);
  }

  @ApiOperation(value = "附件删除", httpMethod = "GET")
  @RequiresRoles("admin")
  @RequestMapping(value = "delete/{id}")
  public ResponseResult delete(@PathVariable("id") String id) {
    annexService.delete(id);
    return ResponseResult.SUCCEED;
  }

  @ApiOperation(value = "附件批量删除", httpMethod = "POST")
  @RequiresRoles("admin")
  @RequestMapping(value = "batch/delete")
  public ResponseResult deleteInBatch(@RequestParam(value = "ids", required = false) String[] ids) {
    annexService.deleteInBatch(ids);
    return ResponseResult.SUCCEED;
  }

}

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
  <title>数据字典管理</title>
</head>
<pluginCss>
  <!-- page specific plugin styles -->
  <link rel="stylesheet" href="${ctx}/static/css/ui.jqgrid.min.css"/>
</pluginCss>
<pageCss>
  <!-- inline styles related to this page -->
  <style>
    .ui-state-disabled {
      color: #BBB !important;
    }

    #btn-search {
      height: 28px;
      margin-bottom: 4px;
    }

    .no-data {
      margin-top: 10px;
    }

    .ui-jqdialog-content .searchFilter select {
      padding: 5px 1px;
      height: 30px;
      line-height: 30px;
    }

    .ui-jqdialog-content .searchFilter .input-elm {
      padding-bottom: 5px;
    }
  </style>
</pageCss>
<body>
<!-- /section:basics/sidebar -->
<div class="main-content">
  <div class="main-content-inner">
    <!-- #section:basics/content.breadcrumbs -->
    <div class="breadcrumbs" id="breadcrumbs">
      <script type="text/javascript">
        try {
          ace.settings.check('breadcrumbs', 'fixed')
        } catch (e) {
        }
      </script>

      <ul class="breadcrumb">
        <li>
          <i class="ace-icon fa fa-home home-icon"></i>
          <a href="${ctx}/admin/index">首页</a>
        </li>
        <li class="active">数据字典管理</li>
      </ul><!-- /.breadcrumb -->
    </div>

    <!-- /section:basics/content.breadcrumbs -->
    <div class="page-content">
      <div class="page-header">
        <h1>
          数据类型管理
        </h1>
      </div><!-- /.page-header -->

      <div class="row">
        <div class="col-xs-12">
          <!-- PAGE CONTENT BEGINS -->
          <div class="row">
            <div class="col-xs-12">
              <form class="form-search" action="">
                <div class="dataTables_filter input-group">
                  <input type="text" id="searchKey" name="searchKey" value="${searchKey}" class="input-sm"
                         placeholder="代码、名称" aria-controls="datatables">
                  <input style="display:none"/>
                  <span class="input-group-btn">
											<button id="btn-search" class="btn btn-xs btn-purple" type="button"><i class="fa fa-search"></i>查询</button>
										</span>
                </div>
              </form>
            </div>
          </div>

          <table id="grid-table"></table>
          <!-- PAGE CONTENT ENDS -->
        </div><!-- /.col -->
      </div><!-- /.row -->
    </div><!-- /.page-content -->
  </div>
</div><!-- /.main-content -->
</body>
<pluginJs>
  <!-- page specific plugin scripts -->
  <script src="${ctx}/static/js/jqGrid/jquery.jqGrid.min.js"></script>
  <script src="${ctx}/static/js/jqGrid/i18n/grid.locale-cn.js"></script>
  <script src="${ctx}/static/js/jquery.jqGrid.foomei.min.js"></script>
</pluginJs>
<pageJs>
  <!-- inline scripts related to this page -->
  <script type="text/javascript">
    var grid_selector = "#grid-table";
    var grid_page_url = "${ctx}/api/dataType/page";
    var grid_add_url = "${ctx}/admin/dataType/create";
    var grid_edit_url = "${ctx}/admin/dataType/update/";
    var grid_del_url = "${ctx}/api/dataType/delete/";
    var grid_data_url = "${ctx}/admin/dataDictionary?typeId=";

    jQuery(function ($) {
      $(grid_selector).foomei_JqGrid({
        url: grid_page_url,
        colNames: ['操作', '代码', '名称', '备注', '数据可修改'],
        colModel: [
          //{name:'myac',index:'', width:80, fixed:true, sortable:false, resize:false,
          //formatter:'actions',
          //formatoptions:{
          //keys:true,
          //delbutton:true, delOptions:{recreateForm:true, beforeShowForm:beforeDeleteCallback},
          //editformbutton:true, editOptions:{recreateForm:true, beforeShowForm:beforeEditCallback}
          //}
          //},
          {
            name: 'myop', index: '', width: 80, fixed: true, sortable: false, resize: false, search: false,
            formatter: function (cellvalue, options, rowObject) {
              return '<div class="action-buttons">'
                + '<div title="编辑" style="float:left;cursor:pointer;" class="ui-pg-div ui-inline-edit" onmouseover="$(this).addClass(\'ui-state-hover\');" onmouseout="$(this).removeClass(\'ui-state-hover\')"><a class="blue" href="' + grid_edit_url + rowObject.id + '"><i class="ace-icon fa fa-pencil bigger-140"></i></a></div>'
                + '<div title="数据字典" style="float:left;cursor:pointer;" class="ui-pg-div ui-inline-edit" onmouseover="$(this).addClass(\'ui-state-hover\');" onmouseout="$(this).removeClass(\'ui-state-hover\')"><a class="orange" href="' + grid_data_url + rowObject.id + '"><i class="ace-icon fa fa-table bigger-140"></i></a></div>'
                + (rowObject.editable ? '<div title="删除" style="float:left;cursor:pointer;" class="ui-pg-div ui-inline-edit" onmouseover="$(this).addClass(\'ui-state-hover\');" onmouseout="$(this).removeClass(\'ui-state-hover\')"><a class="red btn-del" href="javascript:void(0);" data-id="' + rowObject.id + '"><i class="ace-icon fa fa-trash-o bigger-140"></i></a></div>' : '')
                + '</div>';
            }
          },
          {name: 'code', index: 'code', width: 100},
          {name: 'name', index: 'name', width: 100},
          {name: 'remark', index: 'remark', width: 70},
          {name: 'editable', index: 'editable', width: 50, sortable: false, formatter: function (cellvalue, options, rowObject) {
            if (cellvalue) {
              return '<i class="ace-icon fa fa-check-circle orange bigger-140"></i>';
            } else {
              return '<i class="ace-icon fa fa-times-circle grey bigger-140"></i>';
            }
          }}
        ],
        navAdd: [
          {
            caption: '',
            title: '添加',
            buttonicon: 'ace-icon fa fa-plus-circle purple',
            onClickButton: function () {
              window.location.href = grid_add_url;
            },
            position: 'first'
          }
        ]
      });

      $(grid_selector).foomei_JqGrid('resize');

      $("#searchKey").keydown(function (e) {
        if (e.keyCode == 13) {
          $(grid_selector).foomei_JqGrid('search', {
            "searchKey": $('#searchKey').val()
          });
        }
      });

      $("#btn-search").click(function () {
        $(grid_selector).foomei_JqGrid('search', {
          "searchKey": $('#searchKey').val()
        });
      });

      $(document).on('click', ".btn-del", function () {
        var id = $(this).attr("data-id");
        BootstrapDialog.confirm('你确定要删除吗？', function (result) {
          if (result) {
            $.ajax({
              url: grid_del_url + id,
              type: 'GET',
              cache: false,
              dataType: 'json',
              success: function (result) {
                if (result.success) {
                  toastr.success('删除成功');
                  $(grid_selector).foomei_JqGrid('reload');
                } else {
                  toastr.error(result.message);
                }
              },
              error: function () {
                toastr.error('未知错误，请联系管理员');
              }
            });
          }
        });
      });
    })
  </script>
</pageJs>
</html>
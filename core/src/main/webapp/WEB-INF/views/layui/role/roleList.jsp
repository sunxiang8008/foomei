<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>角色管理</title>
  <meta name="renderer" content="webkit">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta name="apple-mobile-web-app-status-bar-style" content="black">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="format-detection" content="telephone=no">
  <link rel="stylesheet" href="${ctx}/static/js/layui/css/layui.css" media="all"/>
  <link rel="stylesheet" href="//at.alicdn.com/t/font_tnyc012u2rlwstt9.css" media="all" />
</head>
<style>
  .kit-table, .kit-table .kit-table-header {
    position: relative;
    box-shadow: 0 1px 7px 0 #ccc;
  }

  .kit-table .kit-table-header {
    height: 50px;
  }

  .kit-table .kit-table-header .kit-search-btns {
    padding: 10px;
    position: absolute;
  }

  .kit-table .kit-table-header .kit-search-inputs {
    position: absolute;
    right: 0px;
    top: 0;
    padding: 10px 25px 10px 10px;
    display: inline;
  }

  .kit-table .kit-table-header .kit-search-inputs .kit-search-keyword {
    margin-right: 10px;
    position: relative;
    display: inline-block;
  }

  .kit-table .kit-table-header .kit-search-inputs .kit-search-keyword input {
    height: 30px;
    line-height: 30px;
    width: 200px;
    padding-right: 32px;
  }

  .kit-table .kit-table-header .kit-search-inputs .kit-search-keyword button {
    position: absolute;
    right: 0;
    top: 0;
    width: 30px;
    height: 30px;
    border: 0;
    cursor: pointer;
    background-color: #009688;
    color: #fff;
  }

  .kit-table .kit-table-header .kit-search-inputs .kit-search-more {
    cursor: pointer;
    color: #009688;
    display: inline-block;
  }

  .kit-table .kit-search-mored {
    width: 100%;
    height: auto;
    top: 51px;
    background-color: #fff;
    z-index: 5;
    box-shadow: 0 4px 7px -3px #ccc;
    position: absolute;
    margin-bottom: 10px;
    display: none;
  }

  .kit-table .kit-search-mored .kit-search-body {
    padding: 10px 10px 45px;
  }

  .kit-table .kit-search-mored .kit-search-footer {
    height: 50px;
    bottom: 0;
    left: 0;
    position: absolute;
    width: 100%;
    border-top: 1px solid #e2e2e2;
    text-align: right;
  }

  .kit-table .kit-search-mored .kit-search-footer .kit-btn {
    margin: 10px 5px;
    padding: 0 15px;
  }

  .kit-table .kit-table-body .layui-table-view {
    margin: 0;
  }
</style>
<body class="childrenBody">
<div class="kit-table">
  <form class="layui-form" lay-filter="kit-search-form">
    <div class="kit-table-header">
      <div class="kit-search-btns">
        <a href="javascript:;" data-action="add" class="layui-btn layui-btn-small"><i class="layui-icon">&#xe608;</i>新增</a>
      </div>
      <div class="kit-search-inputs">
        <div class="kit-search-keyword">
          <input type="text" class="layui-input" name="searchKey" placeholder="搜索关键字.." />
          <button lay-submit lay-filter="search"><i class="layui-icon">&#xe615;</i></button>
        </div>
      </div>
    </div>
  </form>
  <div class="kit-table-body">
    <table id="kit-table" lay-filter="kit-table"></table>
    <script type="text/html" id="kit-table-bar">
      <a class="layui-btn layui-btn-mini" lay-event="edit">编辑</a>
      <a class="layui-btn layui-btn-danger layui-btn-mini" lay-event="del">删除</a>
    </script>
  </div>
</div>
<script src="${ctx}/static/js/layui/layui.js"></script>
</body>
<script>
  var tableId = 'kit-table';
  var tableFilter = 'kit-table';
  var table_page_url = "${ctx}/api/role/list";
  var table_add_url = "${ctx}/layui/role/create";
  var table_edit_url = "${ctx}/layui/role/update/";
  var table_del_url = "${ctx}/api/role/delete/";
  layui.use('table', function () {
    var table = layui.table,
      layer = layui.layer,
      $ = layui.jquery,
      form = layui.form;

    var kitTable = table.render({
      elem: '#' + tableId,
      id: tableId,
      url: table_page_url,
      height: 'full-50',
      cols: [
        [
          { checkbox: true, fixed: true },
          { field: 'id', title: 'ID', width: 80 },
          { field: 'code', title: '代码', width: 100, sort: true },
          { field: 'name', title: '名称', width: 150 },
          { fixed: 'right', title: '操作', width: 180, align: 'center', toolbar: '#kit-table-bar' }
        ]
      ],
      even: true,
      page: true,
      limits: [10, 20, 50, 100],
      limit: 10,
      request: {
        pageName: 'pageNo',
        limitName: 'pageSize'
      },
      response: {
        statusName: 'code',
        statusCode: 0,
        msgName: 'message',
        countName: 'total',
        dataName: 'data'
      }
    });
    //渲染表单
    form.render(null, 'kit-search-form');
    //监听搜索表单提交
    form.on('submit(search)', function (data) {
      kitTable.reload({
        where: data.field
      });
      return false;
    });
    //监听排序
    table.on('sort(' + tableFilter + ')', function (obj) {
      table.reload(tableId, {
        initSort: obj,
        where: {
          sortBy: obj.field,
          sortDir: obj.type
        }
      });
    });
    //监听工具条
    table.on('tool(' + tableFilter + ')', function (obj) {
      var data = obj.data; //获得当前行数据
      var layEvent = obj.event; //获得 lay-event 对应的值
      var tr = obj.tr; //获得当前行 tr 的DOM对象
      //console.log(obj);

      if (layEvent === 'view') { //查看
        //do somehing
      } else if (layEvent === 'del') { //删除
        layer.confirm('你确定要删除吗？', function (index) {
          layer.close(index);
          $.ajax({
            url: table_del_url + data.id,
            type: 'GET',
            cache: false,
            dataType: 'json',
            success: function (result) {
              if (result.success) {
                layer.msg('删除成功', {icon: 1});
                kitTable.reload();
              } else {
                layer.msg(result.message, {icon: 5});
              }
            },
            error: function () {
              layer.msg('未知错误，请联系管理员', {icon: 5});
            }
          });
        });
      } else if (layEvent === 'edit') { //编辑
        var index = layer.open({
          title : "修改角色",
          type : 2,
          content : table_edit_url + data.id,
          success : function(layero, index){
            setTimeout(function(){
              layui.layer.tips('点击此处返回角色列表', '.layui-layer-setwin .layui-layer-close', {
                tips: 3
              });
            },500)
          }
        })
        //改变窗口大小时，重置弹窗的高度，防止超出可视区域（如F12调出debug的操作）
        $(window).resize(function(){
          layer.full(index);
        })
        layer.full(index);
      }
    });
    $('#kit-search-more').on('click', function () {
      $('.kit-search-mored').toggle();
    });

    var tab = parent.tab;
    $('.kit-search-btns > a').off('click').on('click', function () {
      var $that = $(this),
        action = $that.data('action');
      switch (action) {
        case 'add':
          var index = layer.open({
            title : "新增角色",
            type : 2,
            content : table_add_url,
            success : function(layero, index){
              setTimeout(function(){
                layui.layer.tips('点击此处返回角色列表', '.layui-layer-setwin .layui-layer-close', {
                  tips: 3
                });
              },500)
            }
          })
          //改变窗口大小时，重置弹窗的高度，防止超出可视区域（如F12调出debug的操作）
          $(window).resize(function(){
            layer.full(index);
          })
          layer.full(index);
          break;
        case 'batchDel':
          break;
      }
    });
  });
</script>
</html>
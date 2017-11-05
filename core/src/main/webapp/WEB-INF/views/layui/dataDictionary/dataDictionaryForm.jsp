<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>字典管理</title>
  <meta name="renderer" content="webkit">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
  <meta name="apple-mobile-web-app-status-bar-style" content="black">
  <meta name="apple-mobile-web-app-capable" content="yes">
  <meta name="format-detection" content="telephone=no">
  <link rel="stylesheet" href="${ctx}/static/js/layui/css/layui.css" media="all"/>
  <link rel="stylesheet" href="//at.alicdn.com/t/font_tnyc012u2rlwstt9.css" media="all"/>
  <link rel="stylesheet" href="${ctx}/static/js/layui/page.css" media="all"/>
  <style type="text/css">
    .input-required {
      margin-left: 2px;
      color: #c00;
    }
  </style>
</head>
<body class="kit-main">
<form id="form" class="layui-form layui-form-pane" action="${ctx}/api/dataDictionary/save" method="post" style="width:80%;">
  <input type="hidden" name="id" id="id" value="${dataDictionary.id}"/>
  <input type="hidden" name="typeId" id="typeId" value="${type.id}"/>
  <input type="hidden" name="typeCode" id="typeCode" value="${type.code}"/>
  <div class="layui-form-item">
    <label class="layui-form-label">上级</label>
    <div class="layui-input-block">
      <input type="hidden" name="parentId" id="parentId" value="${parent.id}"/>
      <input type="text" name="parentName" id="parentName" value="${parent.name}" placeholder="上级" class="layui-input" disabled>
    </div>
  </div>
  <div class="layui-form-item">
    <label class="layui-form-label">代码<span class="input-required">*</span></label>
    <div class="layui-input-block">
      <input type="text" name="code" value="${dataDictionary.code}" lay-verify="required" placeholder="代码" autocomplete="off" class="layui-input">
    </div>
  </div>
  <div class="layui-form-item">
    <label class="layui-form-label">名称<span class="input-required">*</span></label>
    <div class="layui-input-block">
      <input type="text" name="name" value="${dataDictionary.name}" lay-verify="required" placeholder="名称" autocomplete="off" class="layui-input">
    </div>
  </div>
  <div class="layui-form-item">
    <label class="layui-form-label">序号<span class="input-required">*</span></label>
    <div class="layui-input-block">
      <input type="text" name="priority" value="${dataDictionary.priority}" lay-verify="required" placeholder="序号" autocomplete="off" class="layui-input">
    </div>
  </div>
  <div class="layui-form-item layui-form-text">
    <label class="layui-form-label">备注</label>
    <div class="layui-input-block">
      <textarea name="remark" placeholder='备注' class="layui-textarea">${dataDictionary.remark}</textarea>
    </div>
  </div>
  <div class="layui-form-item">
    <div class="layui-input-block">
      <button class="layui-btn" lay-submit lay-filter="save">保存</button>
      <button class="layui-btn layui-btn-primary btn-close">关闭</button>
    </div>
  </div>
</form>
<script src="${ctx}/static/js/layui/layui.js"></script>
</body>
<script>
  layui.use(['form'], function () {
    var form = layui.form,
      layer = layui.layer,
      $ = layui.jquery;

    //监听提交
    form.on('submit(save)', function (data) {
      var loadIndex = layer.load();
      $.ajax({
        url: data.form.action,
        type: 'POST',
        cache: false,
        data: data.field,
        dataType: 'json',
        success: function (result) {
          if (result.success) {
            loadIndex && layer.close(loadIndex);
            layer.msg('保存成功', {icon: 1});
            parent.layer.closeAll("iframe");
            parent.location.reload();
          } else {
            loadIndex && layer.close(loadIndex);
            if(result.data) {
              var message = '';
              for(var i=0;i<result.data.length;i++) {
                message += result.data[i].errorMsg + '<br>';
              }
              layer.msg(message, {icon: 2});
            } else
              layer.msg(result.message, {icon: 2});
          }
        },
        error: function () {
          loadIndex && layer.close(loadIndex);
          layer.msg('未知错误，请联系管理员', {icon: 5});
        }
      });
      return false;
    });

    $('.btn-close').on('click', function(){
      parent.layer.closeAll("iframe");
      return false;
    });
  });
</script>
</body>
</html>
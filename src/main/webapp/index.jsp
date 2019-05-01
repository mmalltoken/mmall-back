<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<body>
<h2>普通文件上传1</h2>
<form enctype="multipart/form-data" action="/manage/product/upload.do" method="post">
    <input type="file" name="uploadFile" value="上传文件"/>
    <input type="submit" value="提交"/>
</form>
<hr/>
<h2>富文本上传1</h2>
<form enctype="multipart/form-data" action="/manage/product/rich_text_upload.do" method="post">
    <input type="file" name="uploadFile" value="上传文件"/>
    <input type="submit" value="提交"/>
</form>
</body>
</html>
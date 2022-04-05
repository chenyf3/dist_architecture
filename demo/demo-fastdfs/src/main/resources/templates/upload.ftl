<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doUpload" method="post" enctype="multipart/form-data">
        文件：<input type="file" name="uploadFile" /><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>多文件上传</title>
</head>
<body>
    <form action="/demo/doMutUpload" method="post" enctype="multipart/form-data">
        文件_1：<input type="file" name="file_1"><br>
        文件_2：<input type="file" name="file_2"><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
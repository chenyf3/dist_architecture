<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doUpload2" method="post" enctype="multipart/form-data">
        bucket名：<input type="text" name="bucketName" value="test-bucket"/><br>
        文件：<input type="file" name="uploadFile" /><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doOriRotate" method="post" enctype="multipart/form-data">
        文件：<input type="file" name="uploadFile" accept="image/*" /><br>
        旋转角度：<input type="text" name="angle" value="90" /><br>
        图片质量：<input type="text" name="quality" value="0.2" /><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
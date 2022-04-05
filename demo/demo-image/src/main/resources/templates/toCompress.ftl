<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doCompress" method="post" enctype="multipart/form-data">
        文件：<input type="file" name="uploadFile" accept="image/*" /><br>
        压缩比例：<input type="text" name="scale" value="0.3" /><br>
        图片质量：<input type="text" name="quality" value="0.2" /><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
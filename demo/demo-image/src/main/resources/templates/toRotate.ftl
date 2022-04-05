<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doRotate" method="post" enctype="multipart/form-data">
        文件：<input type="file" name="uploadFile" accept="image/*" /><br>
        宽：<input type="text" name="width" value="400" /><br>
        高：<input type="text" name="height" value="300" /><br>
        旋转角度：<input type="text" name="angle" value="90" /><br>
        图片质量：<input type="text" name="quality" value="0.2" /><br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
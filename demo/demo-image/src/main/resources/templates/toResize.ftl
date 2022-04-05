<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>文件上传</title>
</head>
<body>
    <form action="/demo/doResize" method="post" enctype="multipart/form-data">
        文件：<input type="file" name="uploadFile" accept="image/*" /><br>
        宽：<input type="text" name="width" value="400" /><br>
        高：<input type="text" name="height" value="300" /><br>
        保留尺寸比例：
        <label><input type="radio" name="keepRatio" value="1" checked/>是</label>
        <label><input type="radio" name="keepRatio" value="0" />否</label>
        <br>
        <input type="submit" value="提交"/>
    </form>
</body>
</html>
<!DOCTYPE html>
<html lang="zh_CN">
<head>
    <meta charset="UTF-8">
    <title>图片查看</title>
    <script src="https://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
</head>
<body>
    <script>
        $(function(){
            $("#submit_btn").click(function (){
                let fileName = $('input[name=fileName]').val();
                let url = "/demo/getPicPage?fileName=" + fileName;
                $("#img_pre").attr("src", url);
            });
        })
    </script>

    <form action="/demo/getPicPage" method="get">
        文件名：<input type="text" name="fileName" placeholder="请输入要查看的图片名" size="100" />
        <br>
        <input type="button" id="submit_btn" value="提交"/>
    </form>
    <br>
    <img id="img_pre" src="" alt="图片展示">
</body>
</html>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>邮件合并通知</title>
</head>

<body>
<div>
    <h2>邮件合并通知</h2>
    <table id="emailTable">
        <thead>
        <tr>
            <th>序号</th>
            <th>创建时间</th>
            <th>流水号</th>
            <th>邮件内容</th>
        </tr>
        </thead>
        <tbody>
        <#list emailList as email>
            <tr>
                <td>${email_index + 1}</td>
                <td>${(email.createTime?string("yyyy-MM-dd HH:mm:ss"))!""}</td>
                <td>${(email.trxNo)!""}</td>
                <td>${(email.content)!""}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
</body>

<style type="text/css">
    table {
        font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
        width: 100%;
        border-collapse: collapse;
    }

    td, th {
        font-size: 1em;
        border: 1px solid #5B4A42;
        padding: 3px 7px 2px 7px;
    }

    th {
        font-size: 1.1em;
        text-align: center;
        padding-top: 5px;
        padding-bottom: 4px;
        background-color: #24A9E1;
        color: #ffffff;
    }
</style>
</html>
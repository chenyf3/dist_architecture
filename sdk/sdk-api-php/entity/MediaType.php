<?php
namespace xpay;

class MediaType
{
    const APPLICATION_OCTET_STREAM = 'application/octet-stream'; //表示通用的二进制文件
    const APPLICATION_PDF = 'application/pdf';
    const APPLICATION_XML = 'application/xml';
    const APPLICATION_ZIP = 'application/zip';
    const APPLICATION_RAR = 'application/x-rar-compressed';
    const APPLICATION_WORD_O3 = 'application/msword';//.doc文件，03版的word文档
    const APPLICATION_WORD_07 = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'; //.docx文件，07版的word文档
    const APPLICATION_EXCEL_O3 = 'application/vnd.ms-excel'; //.xls文件，03版的excel表格
    const APPLICATION_EXCEL_07 = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'; //.xlsx文件，07版的excel表格
    const APPLICATION_JSON_UTF8 = 'application/json;charset=UTF-8';
    const APPLICATION_FORM_URLENCODED = 'application/x-www-form-urlencoded';
    const IMAGE_JPEG = 'image/jpeg';
    const IMAGE_PNG = 'image/png';
    const IMAGE_GIF = 'image/gif';
    const TEXT_PLAIN = 'text/plain';
    const TEXT_CSV = 'text/csv'; //.csv文件
}
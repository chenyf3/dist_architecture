<?php
namespace xpay;

class FileUtil
{
    /**
     * 读取文件为字节流
     * @param $fileFullName
     * @return false|string
     */
    public static function readFileInBytes(string $fileFullName){
        $handle = fopen($fileFullName, "r");
        try {
            $fileSize = filesize($fileFullName);
            return fread($handle, $fileSize);
        } finally {
            fclose($handle);
        }
    }

    /**
     * 写文件
     * @param string $dir
     * @param string $filename
     * @param $data
     * @return false|int
     */
    public static function writeFile(string $dir, string $filename, $data)
    {
        $filePath = $dir . $filename;
        return file_put_contents($filePath, $data);
    }
}
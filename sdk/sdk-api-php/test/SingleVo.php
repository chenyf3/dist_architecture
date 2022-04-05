<?php
namespace xpay;

/**
 * 样例VO，模拟批量交易的明细业务对象
 * Class SingleVo
 * @package testvo
 */
class SingleVo implements \JsonSerializable {
    private $productName;
    private $productAmount;
    private $count;

    /**
     * 需要实现此方法，以便json_encode()方法能返回私有属性
     * @return array|mixed
     */
    public function jsonSerialize(){
        $vars = get_object_vars($this);
        return $vars;
    }

    /**
     * @return mixed
     */
    public function getProductName()
    {
        return $this->productName;
    }

    /**
     * @param mixed $productName
     */
    public function setProductName($productName): void
    {
        $this->productName = $productName;
    }

    /**
     * @return mixed
     */
    public function getProductAmount()
    {
        return $this->productAmount;
    }

    /**
     * @param mixed $productAmount
     */
    public function setProductAmount($productAmount): void
    {
        $this->productAmount = $productAmount;
    }

    /**
     * @return mixed
     */
    public function getCount()
    {
        return $this->count;
    }

    /**
     * @param mixed $count
     */
    public function setCount($count): void
    {
        $this->count = $count;
    }

}
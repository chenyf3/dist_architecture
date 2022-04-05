<?php
namespace xpay;

/**
 * 样例VO，模拟批量交易的批次业务对象
 * Class BatchVo
 * @package testvo
 */
class BatchVo implements \JsonSerializable {
    private $totalCount;
    private $totalAmount;
    private $detailList;

    /**
     * 需要实现此方法，以便json_encode()方法能返回私有属性
     * @return array|mixed
     */
    public function jsonSerialize(){
        $data = [];
        foreach ($this as $key => $val){
            $data[$key] = $val;
        }
        return $data;
    }

    /**
     * @return mixed
     */
    public function getTotalCount()
    {
        return $this->totalCount;
    }

    /**
     * @param mixed $totalCount
     */
    public function setTotalCount($totalCount): void
    {
        $this->totalCount = $totalCount;
    }

    /**
     * @return mixed
     */
    public function getTotalAmount()
    {
        return $this->totalAmount;
    }

    /**
     * @param mixed $totalAmount
     */
    public function setTotalAmount($totalAmount): void
    {
        $this->totalAmount = $totalAmount;
    }

    /**
     * @return mixed
     */
    public function getDetailList()
    {
        return $this->detailList;
    }

    /**
     * @param mixed $detailList
     */
    public function setDetailList($detailList): void
    {
        $this->detailList = $detailList;
    }


}
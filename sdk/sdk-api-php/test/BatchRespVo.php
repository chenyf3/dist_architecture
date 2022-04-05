<?php
namespace xpay;


class BatchRespVo
{
    private $orderStatus;
    private $totalCount;
    private $singleList;

    /**
     * @return mixed
     */
    public function getOrderStatus()
    {
        return $this->orderStatus;
    }

    /**
     * @param mixed $orderStatus
     */
    public function setOrderStatus($orderStatus): void
    {
        $this->orderStatus = $orderStatus;
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
    public function getSingleList()
    {
        return $this->singleList;
    }

    /**
     * @param mixed $singleList
     */
    public function setSingleList($singleList): void
    {
        $this->singleList = $singleList;
    }
}
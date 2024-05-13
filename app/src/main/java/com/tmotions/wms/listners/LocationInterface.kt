package com.tmotions.wms.listners

interface LocationInterface {
    abstract fun onLocationChange(latitude: Double, longitude: Double)
}
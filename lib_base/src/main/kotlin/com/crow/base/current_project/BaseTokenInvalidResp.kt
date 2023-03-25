package com.crow.base.current_project

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Home page resp
 *
 * @property mCode 响应码
 * @property mMessage 消息
 * @property mResults 结果集
 * @constructor Create empty Home page resp
 */

@JsonClass(generateAdapter = true)
data class BaseTokenInvalidResp (

    @Json(name = "code")
    val mCode: Int,

    @Json(name = "message")
    val mMessage: String,

    @Json(name = "results")
    val mResults: String

)
package com.hexagonkt.server.servlet

import com.hexagonkt.server.EngineResponse
import java.io.OutputStream
import java.net.HttpCookie
import javax.servlet.ServletContext
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class BServletResponse(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val context: ServletContext = req.servletContext) : EngineResponse {

    override val outputStream: OutputStream by lazy { resp.outputStream }

    override var body: Any = ""

    override var status: Int
        get() = resp.status
        set(value) { resp.status = value }

    override var contentType: String?
        get() = resp.contentType
        set(value) { resp.contentType = value }

    override fun getMimeType (file: String): String? = context.getMimeType(file)

    override fun addHeader (name: String, value: String) {
        resp.addHeader(name, value)
    }

    override fun addCookie (cookie: HttpCookie) {
        resp.addCookie(Cookie(cookie.name, cookie.value))
    }

    override fun removeCookie (name: String) {
        val cookie = req.cookies.find { it.name == name }
        if (cookie != null) {
            cookie.value = ""
            cookie.path = "/"
            cookie.maxAge = 0
            resp.addCookie(cookie)
        }
    }

    override fun redirect(url: String) {
        resp.sendRedirect(url)
    }
}

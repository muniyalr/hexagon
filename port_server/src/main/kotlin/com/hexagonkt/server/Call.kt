package com.hexagonkt.server

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.getContentTypeFormat
import java.nio.charset.Charset.defaultCharset
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.Locale.forLanguageTag as localeFor
import com.hexagonkt.settings.SettingsManager.settings

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
class Call(
    val request: Request,
    val response: Response,
    val session: Session,
    /** Call attributes (for the current request). Same as HttpServletRequest.setAttribute(). */
    var attributes: Map<String, Any> = linkedMapOf()) {

    fun redirect(url: String) = response.redirect(url)

    fun ok(content: Any, type: String? = null) = send(200, content, type)
    fun created(content: Any, type: String? = null) = send(201, content, type)
    fun ok(code: Int = 200, content: Any = "", type: String? = null) = send(code, content, type)
    fun error(code: Int = 500, content: Any = "") = send(code, content)

    fun halt(content: Any): Nothing = halt(500, content)
    fun halt(code: Int = 500, content: Any = ""): Nothing {
        throw CodedException(code, content.toString())
    }

    fun contentType(): String =
        response.contentType ?:
        request.headers["Accept"]?.first()?.let { if (it == "*/*") null else it } ?:
        request.contentType ?:
        defaultFormat.contentType

    fun serializationFormat(): SerializationFormat = getContentTypeFormat(contentType())

    fun setContentTypeFor(template: String) {
        if (response.contentType == null) {
            val mimeType = response.getMimeType(template)
            response.contentType = "$mimeType; charset=${defaultCharset().name()}"
        }
    }

    fun fullContext(locale: Locale, context: Map<String, *>): Map<String, *> {
        val extraParameters = mapOf(
            "path" to request.path.removeSuffix("/"), // Do not allow trailing slash
            "lang" to locale.language
        )

        return settings + context + session.attributes + extraParameters
    }

    /**
     * TODO Review order precedence and complete code (now only taking request attribute)
     *
     * 1. Request
     * 2. Session
     * 3. Cookie
     * 4. Accept-language
     * 5. Server default locale
     */
    fun obtainLocale(): Locale = when {
        attributes["lang"] as? String != null -> localeFor(attributes["lang"] as String)
        else -> Locale.getDefault()
    }

    fun httpDate (date: LocalDateTime = now()): String =
        RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))

    private fun send(code: Int, content: Any, contentType: String? = null) {
        response.status = code
        response.body = content

        if (contentType != null)
            response.contentType = contentType
    }
}

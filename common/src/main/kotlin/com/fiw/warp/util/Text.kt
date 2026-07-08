package com.fiw.warp.util

/** Replaces `{key}` placeholders in a message template, e.g. `"Hi {name}".fmt("name" to "Fi")`. */
fun String.fmt(vararg pairs: Pair<String, Any>): String {
	var result = this
	for ((key, value) in pairs) result = result.replace("{$key}", value.toString())
	return result
}

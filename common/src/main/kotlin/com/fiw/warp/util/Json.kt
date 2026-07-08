package com.fiw.warp.util

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/** Single shared Gson instance. Gson ships with Minecraft, so no extra dependency. */
object Json {
	val GSON: Gson = GsonBuilder()
		.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create()
}

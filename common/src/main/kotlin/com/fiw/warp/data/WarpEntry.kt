package com.fiw.warp.data

import java.util.UUID

/** A stored warp: its location plus the player who created it (null = admin/legacy warp). */
data class WarpEntry(val location: WarpLocation, val owner: UUID? = null)

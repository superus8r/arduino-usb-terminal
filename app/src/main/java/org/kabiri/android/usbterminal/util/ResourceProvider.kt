package org.kabiri.android.usbterminal.util

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ali Kabiri on 07.05.2025.
 */

fun interface IResourceProvider {
    fun getString(resId: Int): String
}

@Singleton
class ResourceProvider
@Inject constructor(
    private val context: Context,
) : IResourceProvider {
    override fun getString(resId: Int): String = context.getString(resId)
}

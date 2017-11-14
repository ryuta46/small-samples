package com.ryuta46.nemapitestandroid.entity

data class MosaicDefinition(
        val creator: String,
        val id: MosaicId,
        val description: String,
        val properties: List<MosaicProperty>
) {
    val divisibility: Int
        get() = properties.find { it.name == "divisibility" } !!.value.toInt()

    val initialSupply: Long
        get() = properties.find { it.name == "initialSupply" } !!.value.toLong()

    val supplyMutable: Boolean
        get() = properties.find { it.name == "supplyMutable" } !!.value.toBoolean()

    val transferable: Boolean
        get() = properties.find { it.name == "transferable" } !!.value.toBoolean()
}

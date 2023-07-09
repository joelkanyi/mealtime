package com.joelkanyi.shared.`data`.local

import kotlin.String

public data class OnlineMealCategoryEntity(
  public val idCategory: String,
  public val strCategory: String,
  public val strCategoryDescription: String,
  public val strCategoryThumb: String
) {
  public override fun toString(): String = """
  |OnlineMealCategoryEntity [
  |  idCategory: $idCategory
  |  strCategory: $strCategory
  |  strCategoryDescription: $strCategoryDescription
  |  strCategoryThumb: $strCategoryThumb
  |]
  """.trimMargin()
}

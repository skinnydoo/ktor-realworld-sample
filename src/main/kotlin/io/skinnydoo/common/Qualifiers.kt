package io.skinnydoo.common

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

object IODispatcher : Qualifier {
  override val value: QualifierValue = "IO"
}

object DefaultDispatcher : Qualifier {
  override val value: QualifierValue = "Default"
}

package io.skinnydoo.graphql

import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import org.dataloader.DataLoaderRegistry

class KtorDataLoaderRegistryFactory : DataLoaderRegistryFactory {

  override fun generate(): DataLoaderRegistry {
    val registry = DataLoaderRegistry()
    return registry
  }
}
